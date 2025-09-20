package com.form.form_back.Repo;

import com.form.form_back.Entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // ✅ NOUVEAU : Utilisateurs avec leurs groupes
    @Query("SELECT DISTINCT u FROM Utilisateur u " +
            "LEFT JOIN FETCH u.groups " +
            "LEFT JOIN FETCH u.assignedGroup " +
            "WHERE u.id = :id")
    Optional<Utilisateur> findByIdWithGroups(@Param("id") Long id);

    // ✅ NOUVEAU : Utilisateurs d'un groupe
    @Query("SELECT u FROM Utilisateur u JOIN u.groups g WHERE g.id = :groupId")
    List<Utilisateur> findByGroupId(@Param("groupId") Long groupId);

    // ✅ NOUVEAU : Utilisateurs actifs (non bannis/suspendus)
    @Query("SELECT u FROM Utilisateur u " +
            "WHERE (u.banned = false OR u.banned IS NULL) " +
            "AND (u.suspended = false OR u.suspended IS NULL)")
    List<Utilisateur> findActiveUsers();
}