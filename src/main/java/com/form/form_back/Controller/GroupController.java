package com.form.form_back.Controller;

import com.form.form_back.Entity.Group;
import com.form.form_back.Entity.Utilisateur;
import com.form.form_back.Repo.GroupRepository;
import com.form.form_back.Repo.UtilisateurRepository;
import com.form.form_back.Service.AuthService;
import com.form.form_back.dto.ApiResponse;
import com.form.form_back.dto.GroupDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "http://localhost:4200")
public class GroupController {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private AuthService authService;

    private static final Logger logger = LoggerFactory.getLogger(GroupController.class);

    // ✅ Obtenir tous les groupes actifs (pour la sélection dans le form builder)
    @GetMapping("/active")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<GroupDTO>>> getAllActiveGroups() {
        try {
            List<Group> activeGroups = groupRepository.findByActiveTrue();
            List<GroupDTO> groupDTOs = activeGroups.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            logger.debug("Récupération de {} groupes actifs", groupDTOs.size());

            return ResponseEntity.ok(new ApiResponse<>(
                    "Groupes actifs récupérés avec succès",
                    groupDTOs,
                    true
            ));
        } catch (Exception e) {
            logger.error("Erreur récupération groupes actifs: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Erreur lors de la récupération des groupes: " + e.getMessage(),
                    null,
                    false
            ));
        }
    }

    // ✅ Obtenir tous les groupes
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<GroupDTO>>> getAllGroups() {
        try {
            List<Group> groups = groupRepository.findAll();
            List<GroupDTO> groupDTOs = groups.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            logger.debug("Récupération de {} groupes au total", groupDTOs.size());

            return ResponseEntity.ok(new ApiResponse<>(
                    "Groupes récupérés avec succès",
                    groupDTOs,
                    true
            ));
        } catch (Exception e) {
            logger.error("Erreur récupération groupes: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Erreur lors de la récupération des groupes: " + e.getMessage(),
                    null,
                    false
            ));
        }
    }

    // ✅ Obtenir les groupes de l'utilisateur actuel
    @GetMapping("/user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<GroupDTO>>> getUserGroups() {
        try {
            Long currentUserId = authService.getCurrentUserId();
            Utilisateur user = utilisateurRepository.findById(currentUserId)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            List<GroupDTO> userGroups = user.getGroups().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            logger.debug("Récupération de {} groupes pour l'utilisateur {}",
                    userGroups.size(), user.getUsername());

            return ResponseEntity.ok(new ApiResponse<>(
                    "Groupes de l'utilisateur récupérés avec succès",
                    userGroups,
                    true
            ));
        } catch (Exception e) {
            logger.error("Erreur récupération groupes utilisateur: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Erreur lors de la récupération des groupes: " + e.getMessage(),
                    null,
                    false
            ));
        }
    }

    // ✅ Obtenir un groupe par ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GroupDTO>> getGroupById(@PathVariable Long id) {
        try {
            Group group = groupRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Groupe non trouvé"));

            GroupDTO groupDTO = convertToDTO(group);

            return ResponseEntity.ok(new ApiResponse<>(
                    "Groupe récupéré avec succès",
                    groupDTO,
                    true
            ));
        } catch (Exception e) {
            logger.error("Erreur récupération groupe {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Erreur: " + e.getMessage(),
                    null,
                    false
            ));
        }
    }

    // ✅ Créer un nouveau groupe (admin uniquement)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GroupDTO>> createGroup(@RequestBody GroupCreateRequest request) {
        try {
            Group group = new Group();
            group.setName(request.getName());
            group.setDescription(request.getDescription());
            group.setColor(request.getColor());
            group.setActive(request.getActive() != null ? request.getActive() : true);

            Group savedGroup = groupRepository.save(group);

            logger.info("Nouveau groupe créé: {}", savedGroup.getName());

            return ResponseEntity.ok(new ApiResponse<>(
                    "Groupe créé avec succès",
                    convertToDTO(savedGroup),
                    true
            ));
        } catch (Exception e) {
            logger.error("Erreur création groupe: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Erreur lors de la création: " + e.getMessage(),
                    null,
                    false
            ));
        }
    }

    // ✅ Mettre à jour un groupe (admin uniquement)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GroupDTO>> updateGroup(
            @PathVariable Long id,
            @RequestBody GroupUpdateRequest request) {
        try {
            Group group = groupRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Groupe non trouvé"));

            group.setName(request.getName());
            group.setDescription(request.getDescription());
            group.setColor(request.getColor());
            if (request.getActive() != null) {
                group.setActive(request.getActive());
            }

            Group updatedGroup = groupRepository.save(group);

            logger.info("Groupe mis à jour: {}", updatedGroup.getName());

            return ResponseEntity.ok(new ApiResponse<>(
                    "Groupe mis à jour avec succès",
                    convertToDTO(updatedGroup),
                    true
            ));
        } catch (Exception e) {
            logger.error("Erreur mise à jour groupe {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Erreur lors de la mise à jour: " + e.getMessage(),
                    null,
                    false
            ));
        }
    }

    // ✅ Supprimer un groupe (admin uniquement)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable Long id) {
        try {
            Group group = groupRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Groupe non trouvé"));

            // Vérifier si le groupe est utilisé dans des formulaires
            if (group.getForms() != null && !group.getForms().isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(
                        "Impossible de supprimer ce groupe car il est assigné à des formulaires",
                        null,
                        false
                ));
            }

            groupRepository.deleteById(id);

            logger.info("Groupe supprimé: {}", group.getName());

            return ResponseEntity.ok(new ApiResponse<>(
                    "Groupe supprimé avec succès",
                    null,
                    true
            ));
        } catch (Exception e) {
            logger.error("Erreur suppression groupe {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Erreur lors de la suppression: " + e.getMessage(),
                    null,
                    false
            ));
        }
    }

    // ✅ Méthode utilitaire pour convertir Group en GroupDTO
    private GroupDTO convertToDTO(Group group) {
        GroupDTO dto = new GroupDTO();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setDescription(group.getDescription());
        dto.setColor(group.getColor());
        dto.setActive(group.getActive());
        return dto;
    }

    // ✅ Classes de request pour créer/modifier les groupes
    public static class GroupCreateRequest {
        private String name;
        private String description;
        private String color;
        private Boolean active;

        // Getters et setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }

        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
    }

    public static class GroupUpdateRequest {
        private String name;
        private String description;
        private String color;
        private Boolean active;

        // Getters et setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }

        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
    }
}