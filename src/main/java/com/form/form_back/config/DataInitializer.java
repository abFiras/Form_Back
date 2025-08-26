package com.form.form_back.config;

import com.form.form_back.Entity.ERole;
import com.form.form_back.Entity.Role;
import com.form.form_back.Entity.Utilisateur;
import com.form.form_back.Repo.RoleRepository;
import com.form.form_back.Repo.UtilisateurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(RoleRepository roleRepository,
                                   UtilisateurRepository userRepository,
                                   PasswordEncoder encoder) {
        return args -> {
            // 1️⃣ Vérifier et créer les rôles
            if (!roleRepository.existsByName(ERole.ROLE_ADMIN)) {
                Role adminRole = new Role();
                adminRole.setName(ERole.ROLE_ADMIN);
                roleRepository.save(adminRole);
            }

            if (!roleRepository.existsByName(ERole.ROLE_USER)) {
                Role userRole = new Role();
                userRole.setName(ERole.ROLE_USER);
                roleRepository.save(userRole);
            }

            // 2️⃣ Vérifier et créer un admin par défaut
            if (!userRepository.existsByUsername("admin")) {
                Utilisateur admin = new Utilisateur(
                        "admin",                         // username
                        "admin@example.com",             // email
                        "Super",                         // prénom
                        "Admin",                         // nom
                        encoder.encode("admin123")       // password
                );

                Set<Role> roles = new HashSet<>();
                Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("Role ADMIN not found"));
                roles.add(adminRole);

                admin.setRoles(roles);
                admin.setPhone("00000000"); // si tu veux

                userRepository.save(admin);
                System.out.println("✅ Admin user created with username=admin, password=admin123");
            }
        };
    }
}
