package com.form.form_back.Controller;


import com.form.form_back.Entity.ERole;
import com.form.form_back.Entity.Role;
import com.form.form_back.Entity.Utilisateur;
import com.form.form_back.IService.AdminServices;
import com.form.form_back.Repo.RoleRepository;
import com.form.form_back.Repo.UtilisateurRepository;
import com.form.form_back.dto.SignupDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @PostMapping("/create-user")
    @PreAuthorize("hasRole('ROLE_ADMIN')") // ✅ accessible uniquement aux admins
    public ResponseEntity<?> createUser(@Valid @RequestBody SignupDto signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Email is already in use!");
        }

        // Create new user's account
        Utilisateur user = new Utilisateur();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(encoder.encode(signupRequest.getPassword()));
        user.setPhone(signupRequest.getPhone());

        // Assign ROLE_USER by default
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));
        user.setRoles(Collections.singleton(userRole));

        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User created successfully!");
        return ResponseEntity.ok(response);
    }

    @Operation(description = "getAllUsers")
    @GetMapping(path = "/getAllUsers")
    List<Utilisateur> getAllUsers() {
        return adminServices.getall();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Utilisateur> updateUser(@PathVariable Long id, @RequestBody Utilisateur updatedUser) {
        Utilisateur user = adminServices.updateUser(id, updatedUser);
        return ResponseEntity.ok(user);
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