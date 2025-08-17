package com.form.form_back.Repo;

import com.form.form_back.Entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
    Optional<Utilisateur> findByEmail(String email);

    // Ajouter cette ligne pour le token
    Optional<Utilisateur> findByResetPasswordToken(String token);
    List<Utilisateur> findAllByBanned(boolean banned);
}