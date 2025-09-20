package com.form.form_back.Controller;


import com.form.form_back.Entity.ERole;
import com.form.form_back.Entity.Role;
import com.form.form_back.Entity.Utilisateur;
import com.form.form_back.Repo.RoleRepository;
import com.form.form_back.Repo.UtilisateurRepository;
import com.form.form_back.Security.JwtUtils;
import com.form.form_back.Security.UserDetailsImpl;
import com.form.form_back.Service.EmailService;
import com.form.form_back.dto.LoginDto;
import com.form.form_back.dto.SignupDto;
import com.form.form_back.dto.UserDTO;
import com.form.form_back.response.JwtResponse;
import com.form.form_back.response.MessageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UtilisateurRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non connecté");
        }

        String username = authentication.getName();
        Utilisateur user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        return ResponseEntity.ok(new UserDTO(user)); // 🔥 retour propre
    }



    @Operation(description = "signin")
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginDto loginDto) {
        logger.info("Attempting to authenticate user: {}", loginDto.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Vérifier si l'utilisateur est banni
        if (userDetails.isBanned()) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Votre compte est banni. Contactez l'administrateur."));
        }

        String jwt = jwtUtils.generateJwtToken(authentication);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles,
                userDetails.getPhone()));
    }


    @Operation(description = "signup")
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupDto signupDto) {
        if (userRepository.existsByUsername(signupDto.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signupDto.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        Utilisateur user = new Utilisateur(signupDto.getUsername(),
                signupDto.getEmail(),
                signupDto.getPrenom(),
                signupDto.getNom(),
                encoder.encode(signupDto.getPassword()));

        user.setPhone(signupDto.getPhone());

        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @Autowired
    private EmailService emailService;

    @PostMapping("/reset-password-request")
    public ResponseEntity<?> resetPasswordRequest(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        Optional<Utilisateur> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Email non trouvé");
        }

        Utilisateur user = userOpt.get();
        String resetToken = UUID.randomUUID().toString();
        user.setResetPasswordToken(resetToken);
        user.setResetPasswordExpiry(new Date(System.currentTimeMillis() + 3600_000)); // 1h de validité
        userRepository.save(user);

        // ENVOI DE L'EMAIL
        emailService.sendResetPasswordEmail(user.getEmail(), resetToken);

        // Pour Postman : renvoyer quand même le token pour tester sans frontend
        return ResponseEntity.ok(Map.of(
                "message", "Lien de réinitialisation envoyé à l'email",
                "token", resetToken
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("password");

        Optional<Utilisateur> userOpt = userRepository.findByResetPasswordToken(token);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Token invalide ou expiré");
        }

        Utilisateur user = userOpt.get();

        if (user.getResetPasswordExpiry().before(new Date())) {
            return ResponseEntity.badRequest().body("Token expiré");
        }

        user.setPassword(encoder.encode(newPassword));
        user.setResetPasswordToken(null); // supprime le token
        user.setResetPasswordExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès"));
    }
// Ajoutez ces méthodes à votre AuthController existant

    @PostMapping("/update-profile")
    public ResponseEntity<?> updateProfile(Authentication authentication, @RequestBody Map<String, String> updates) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non connecté");
        }

        String username = authentication.getName();
        Utilisateur user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Mise à jour des champs si présents
        if (updates.containsKey("username")) {
            String newUsername = updates.get("username");
            if (userRepository.existsByUsername(newUsername) && !newUsername.equals(user.getUsername())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Ce nom d'utilisateur est déjà pris"));
            }
            user.setUsername(newUsername);
        }

        if (updates.containsKey("email")) {
            String newEmail = updates.get("email");
            if (userRepository.existsByEmail(newEmail) && !newEmail.equals(user.getEmail())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Cette adresse email est déjà utilisée"));
            }
            user.setEmail(newEmail);
        }
        if (updates.containsKey("phone")) {
            user.setPhone(updates.get("phone"));
        }
        if (updates.containsKey("prenom")) {
            user.setPrenom(updates.get("prenom"));
        }
        if (updates.containsKey("nom")) {
            user.setNom(updates.get("nom"));
        }

        // Enregistrer les modifications dans la base
        userRepository.save(user);

        // Créer et renvoyer un DTO sécurisé pour Angular
        UserDTO dto = new UserDTO(user);
        return ResponseEntity.ok(dto);
    }


    @PostMapping("/update-profile-photo")
    public ResponseEntity<?> updateProfilePhoto(Authentication authentication, @RequestBody Map<String, String> request) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non connecté");
        }

        String username = authentication.getName();
        Utilisateur user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        String photoUrl = request.get("photoUrl");
        if (photoUrl == null || photoUrl.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("URL de la photo requise");
        }

        user.setProfilePhotoUrl(photoUrl);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Photo de profil mise à jour avec succès",
                "photoUrl", photoUrl
        ));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(Authentication authentication, @RequestBody Map<String, String> request) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non connecté");
        }

        String username = authentication.getName();
        Utilisateur user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Mot de passe actuel et nouveau mot de passe requis");
        }

        // Vérifier le mot de passe actuel
        if (!encoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body("Mot de passe actuel incorrect");
        }

        // Valider le nouveau mot de passe
        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body("Le nouveau mot de passe doit contenir au moins 6 caractères");
        }

        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Mot de passe modifié avec succès!"));
    }

}
