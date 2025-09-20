package com.form.form_back.Repo;

import com.form.form_back.Entity.Form;
import com.form.form_back.Entity.FormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FormRepository extends JpaRepository<Form, Long> {
    List<Form> findByStatusOrderByCreatedAtDesc(String status);





    // ✅ CORRECTION CRITIQUE : Utiliser DISTINCT pour éviter les doublons causés par les JOINs
    @Query("SELECT DISTINCT f FROM Form f " +
            "LEFT JOIN FETCH f.fields fields " +
            "LEFT JOIN FETCH f.assignedGroups groups " +
            "LEFT JOIN FETCH f.createdBy " +
            "WHERE f.id = :id")
    Form findByIdWithFields(@Param("id") Long id);

    // ✅ Requête corrigée pour getAllWithGroupsAndCreator
    @Query("SELECT DISTINCT f FROM Form f " +
            "LEFT JOIN FETCH f.assignedGroups " +
            "LEFT JOIN FETCH f.createdBy " +
            "ORDER BY f.createdAt DESC")
    List<Form> findAllWithGroupsAndCreator();

    // ✅ Requête corrigée pour findByStatusWithGroups
    @Query("SELECT DISTINCT f FROM Form f " +
            "LEFT JOIN FETCH f.assignedGroups " +
            "LEFT JOIN FETCH f.createdBy " +
            "WHERE f.status = :status " +
            "ORDER BY f.createdAt DESC")
    List<Form> findByStatusWithGroups(@Param("status") String status);

    // ✅ Requête corrigée pour findByCreatedByIdWithGroups
    @Query("SELECT DISTINCT f FROM Form f " +
            "LEFT JOIN FETCH f.assignedGroups " +
            "LEFT JOIN FETCH f.createdBy " +
            "WHERE f.createdBy.id = :userId " +
            "ORDER BY f.createdAt DESC")
    List<Form> findByCreatedByIdWithGroups(@Param("userId") Long userId);

    // ✅ Requête simple sans JOIN pour éviter les doublons
    List<Form> findByCreatedById(Long userId);

    List<Form> findByStatus(String status);

    // ✅ NOUVELLE REQUÊTE : Récupérer un formulaire avec ses champs uniquement (sans groupes)
    @Query("SELECT DISTINCT f FROM Form f " +
            "LEFT JOIN FETCH f.fields " +
            "LEFT JOIN FETCH f.createdBy " +
            "WHERE f.id = :id")
    Optional<Form> findByIdWithFieldsOnly(@Param("id") Long id);

    // ✅ NOUVELLE REQUÊTE : Récupérer un formulaire avec ses groupes uniquement (sans champs)
    @Query("SELECT DISTINCT f FROM Form f " +
            "LEFT JOIN FETCH f.assignedGroups " +
            "LEFT JOIN FETCH f.createdBy " +
            "WHERE f.id = :id")
    Optional<Form> findByIdWithGroupsOnly(@Param("id") Long id);

    // ✅ Formulaires accessibles par un utilisateur (par groupes)
    @Query("SELECT DISTINCT f FROM Form f " +
            "LEFT JOIN FETCH f.assignedGroups ag " +
            "LEFT JOIN FETCH f.createdBy " +
            "WHERE f.id IN (" +
            "    SELECT f2.id FROM Form f2 " +
            "    LEFT JOIN f2.assignedGroups ag2 " +
            "    WHERE ag2 IS NULL " + // Formulaires sans groupes (publics)
            "    OR ag2.id IN :groupIds " + // Formulaires des groupes de l'utilisateur
            "    OR f2.createdBy.id = :userId" + // Formulaires créés par l'utilisateur
            ") ORDER BY f.createdAt DESC")
    List<Form> findAccessibleFormsByUser(@Param("userId") Long userId, @Param("groupIds") List<Long> groupIds);

    // ✅ Formulaires créés par un utilisateur
    @Query("SELECT f FROM Form f WHERE f.createdBy.id = :userId ORDER BY f.createdAt DESC")
    List<Form> findByCreatedBy(@Param("userId") Long userId);

    // ✅ Formulaires d'un groupe spécifique
    @Query("SELECT DISTINCT f FROM Form f " +
            "JOIN f.assignedGroups ag " +
            "WHERE ag.id = :groupId " +
            "ORDER BY f.createdAt DESC")
    List<Form> findByAssignedGroup(@Param("groupId") Long groupId);

    // ✅ Compter les formulaires par statut
    @Query("SELECT COUNT(f) FROM Form f WHERE f.status = :status")
    Long countByStatus(@Param("status") String status);

    // ✅ Formulaires récents (30 derniers jours)
    @Query("SELECT f FROM Form f WHERE f.createdAt >= :dateFrom ORDER BY f.createdAt DESC")
    List<Form> findRecentForms(@Param("dateFrom") LocalDateTime dateFrom);
}