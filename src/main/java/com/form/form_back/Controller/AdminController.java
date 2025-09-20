package com.form.form_back.Controller;

import com.form.form_back.Entity.ERole;
import com.form.form_back.Entity.Group;
import com.form.form_back.Entity.Role;
import com.form.form_back.Entity.Utilisateur;
import com.form.form_back.IService.AdminServices;
import com.form.form_back.Repo.RoleRepository;
import com.form.form_back.Repo.UtilisateurRepository;
import com.form.form_back.Service.GroupService;
import com.form.form_back.dto.GroupDTO;
import com.form.form_back.dto.SignupDto;
import com.form.form_back.dto.UpdateUserDTO;
import com.form.form_back.dto.UserDTO;
import com.form.form_back.response.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin")
public class AdminController {
    @Autowired
    AdminServices adminServices;
    @Autowired
    UtilisateurRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    private GroupService groupService;

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    // DTO POUR LA CRÉATION D'UTILISATEUR AVEC GROUPE MANUEL
    public static class CreateUserRequest extends SignupDto {
        private Long selectedGroupId;

        public Long getSelectedGroupId() {
            return selectedGroupId;
        }

        public void setSelectedGroupId(Long selectedGroupId) {
            this.selectedGroupId = selectedGroupId;
        }
    }

    @PostMapping("/create-user")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest signupRequest) {
        // Vérifications existantes
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Créer le nouvel utilisateur
        Utilisateur user = new Utilisateur();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPrenom(signupRequest.getPrenom());
        user.setNom(signupRequest.getNom());
        user.setPassword(encoder.encode(signupRequest.getPassword()));
        user.setPhone(signupRequest.getPhone());

        // Gestion des rôles
        Set<String> strRoles = signupRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "ROLE_ADMIN":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role ADMIN is not found."));
                        roles.add(adminRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);

        // GESTION MANUELLE VS AUTOMATIQUE DU GROUPE
        Group assignedGroup = null;
        try {
            if (signupRequest.getSelectedGroupId() != null) {
                // Assignation MANUELLE - groupe spécifique sélectionné
                Optional<Group> selectedGroupOpt = groupService.getGroupById(signupRequest.getSelectedGroupId());
                if (selectedGroupOpt.isPresent()) {
                    Group selectedGroup = selectedGroupOpt.get();
                    user.setAssignedGroup(selectedGroup);
                    user.addGroup(selectedGroup);
                    assignedGroup = selectedGroup;
                    logger.info("Assignation MANUELLE: Utilisateur {} assigné au groupe {}",
                            user.getUsername(), selectedGroup.getName());
                } else {
                    logger.warn("Groupe sélectionné non trouvé (ID: {}), utilisation de l'assignation automatique",
                            signupRequest.getSelectedGroupId());
                    assignedGroup = groupService.assignUserToNextAvailableGroup(user);
                }
            } else {
                // Assignation AUTOMATIQUE - groupe avec le moins de membres
                assignedGroup = groupService.assignUserToNextAvailableGroup(user);
                logger.info("Assignation AUTOMATIQUE: Utilisateur {} assigné au groupe {}",
                        user.getUsername(),
                        assignedGroup != null ? assignedGroup.getName() : "Aucun");
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'assignation du groupe pour l'utilisateur {}: {}",
                    user.getUsername(), e.getMessage());
            // Continue même si l'assignation échoue
        }

        // Sauvegarder l'utilisateur
        Utilisateur savedUser = userRepository.save(user);

        // RÉPONSE DÉTAILLÉE
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Utilisateur créé avec succès !");
        response.put("userId", savedUser.getId());
        response.put("username", savedUser.getUsername());
        response.put("assignmentType", signupRequest.getSelectedGroupId() != null ? "MANUAL" : "AUTOMATIC");

        if (savedUser.getAssignedGroup() != null) {
            Map<String, Object> groupInfo = new HashMap<>();
            groupInfo.put("id", savedUser.getAssignedGroup().getId());
            groupInfo.put("name", savedUser.getAssignedGroup().getName());
            groupInfo.put("color", savedUser.getAssignedGroup().getColor());
            groupInfo.put("description", savedUser.getAssignedGroup().getDescription());
            response.put("assignedGroup", groupInfo);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/groups")
    public ResponseEntity<List<GroupDTO>> getAllGroups() {
        List<GroupDTO> groups = groupService.getAllGroups();
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/groups/active")
    public ResponseEntity<List<GroupDTO>> getActiveGroups() {
        List<GroupDTO> groups = groupService.getActiveGroups();
        return ResponseEntity.ok(groups);
    }

    @PostMapping("/users/{userId}/assign-group/{groupId}")
    public ResponseEntity<?> assignUserToGroup(@PathVariable Long userId, @PathVariable Long groupId) {
        try {
            Optional<Utilisateur> userOpt = userRepository.findById(userId);
            Optional<Group> groupOpt = groupService.getGroupById(groupId);

            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Utilisateur non trouvé"));
            }

            if (!groupOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Groupe non trouvé"));
            }

            Utilisateur user = userOpt.get();
            Group group = groupOpt.get();

            // Retirer l'ancien groupe s'il existe
            if (user.getAssignedGroup() != null) {
                user.removeGroup(user.getAssignedGroup());
            }

            // Assigner le nouveau groupe
            user.setAssignedGroup(group);
            user.addGroup(group);
            userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", String.format("Utilisateur %s assigné au groupe %s",
                    user.getUsername(), group.getName()));
            response.put("userId", user.getId());
            response.put("groupId", group.getId());
            response.put("groupName", group.getName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Erreur lors de l'assignation du groupe: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Erreur lors de l'assignation du groupe"));
        }
    }

    // NOUVEAU ENDPOINT POUR RETIRER UN UTILISATEUR D'UN GROUPE
    @PostMapping("/users/{userId}/remove-group")
    public ResponseEntity<?> removeUserFromGroup(@PathVariable Long userId) {
        try {
            Optional<Utilisateur> userOpt = userRepository.findById(userId);

            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Utilisateur non trouvé"));
            }

            Utilisateur user = userOpt.get();

            if (user.getAssignedGroup() != null) {
                Group oldGroup = user.getAssignedGroup();
                user.removeGroup(oldGroup);
                user.setAssignedGroup(null);
                userRepository.save(user);

                Map<String, String> response = new HashMap<>();
                response.put("message", String.format("Utilisateur %s retiré du groupe %s",
                        user.getUsername(), oldGroup.getName()));

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("L'utilisateur n'appartient à aucun groupe"));
            }

        } catch (Exception e) {
            logger.error("Erreur lors de la suppression du groupe: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Erreur lors de la suppression du groupe"));
        }
    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<Utilisateur> users = adminServices.getall();
        List<UserDTO> dtos = users.stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // FIX: Updated method that uses UpdateUserDTO instead of Utilisateur entity
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UpdateUserDTO updateRequest) {
        try {
            // Find the existing user
            Optional<Utilisateur> existingUserOpt = userRepository.findById(id);
            if (!existingUserOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Utilisateur non trouvé"));
            }

            Utilisateur existingUser = existingUserOpt.get();

            // Update basic fields
            if (updateRequest.getUsername() != null) {
                existingUser.setUsername(updateRequest.getUsername());
            }
            if (updateRequest.getEmail() != null) {
                existingUser.setEmail(updateRequest.getEmail());
            }
            if (updateRequest.getPrenom() != null) {
                existingUser.setPrenom(updateRequest.getPrenom());
            }
            if (updateRequest.getNom() != null) {
                existingUser.setNom(updateRequest.getNom());
            }
            if (updateRequest.getPhone() != null) {
                existingUser.setPhone(updateRequest.getPhone());
            }
            if (updateRequest.getSuspended() != null) {
                existingUser.setSuspended(updateRequest.getSuspended());
            }

            // Update roles if provided
            if (updateRequest.getRoles() != null && !updateRequest.getRoles().isEmpty()) {
                Set<Role> roles = new HashSet<>();
                for (String roleName : updateRequest.getRoles()) {
                    try {
                        ERole eRole = ERole.valueOf(roleName);
                        Optional<Role> role = roleRepository.findByName(eRole);
                        if (role.isPresent()) {
                            roles.add(role.get());
                        }
                    } catch (IllegalArgumentException e) {
                        logger.warn("Invalid role name: {}", roleName);
                    }
                }
                existingUser.setRoles(roles);
            }

            // Save the updated user first
            Utilisateur savedUser = userRepository.save(existingUser);

            // Handle group assignment if selectedGroupId is provided and different from current
            if (updateRequest.getSelectedGroupId() != null) {
                Long currentGroupId = savedUser.getAssignedGroup() != null ? savedUser.getAssignedGroup().getId() : null;

                if (!updateRequest.getSelectedGroupId().equals(currentGroupId)) {
                    Optional<Group> newGroupOpt = groupService.getGroupById(updateRequest.getSelectedGroupId());
                    if (newGroupOpt.isPresent()) {
                        Group newGroup = newGroupOpt.get();

                        // Remove from old group
                        if (savedUser.getAssignedGroup() != null) {
                            savedUser.removeGroup(savedUser.getAssignedGroup());
                        }

                        // Add to new group
                        savedUser.setAssignedGroup(newGroup);
                        savedUser.addGroup(newGroup);
                        savedUser = userRepository.save(savedUser);
                    }
                }
            }

            // Return the updated user as DTO
            UserDTO responseDTO = new UserDTO(savedUser);
            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour de l'utilisateur {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Erreur lors de la mise à jour de l'utilisateur"));
        }
    }

    @Operation(description = "getAllRole")
    @GetMapping(path = "/getAllRole")
    List<Role> getAllRole() {
        return adminServices.getAllROles();
    }

    @Operation(description = "Ban a User")
    @PostMapping("/banUser")
    public String banUser(@RequestBody String email) {
        return adminServices.banUser(email);
    }

    @Operation(description = "Suspend a User")
    @PostMapping("/suspendUser")
    public String suspendUser(@RequestBody String email) {
        return adminServices.suspendUser(email);
    }

    @Operation(description = "Unban a User")
    @PostMapping("/unbanUser")
    public String unbanUser(@RequestBody String email) {
        return adminServices.unbanUser(email);
    }

    @Operation(description = "Automatic Unban of Users")
    @PostMapping("/automaticUnbanUser")
    public String automaticUnbanUser() {
        return adminServices.automaticUnbanUser();
    }

    @Operation(description = "Delete a User")
    @DeleteMapping("/deleteUser")
    public void deleteUser(@RequestBody String email) {
        adminServices.deleteUser(email);
    }
}