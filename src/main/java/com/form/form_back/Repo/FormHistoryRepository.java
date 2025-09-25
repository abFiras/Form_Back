package com.form.form_back.Repo;

import com.form.form_back.Entity.FormHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FormHistoryRepository extends JpaRepository<FormHistory, Long> {

    /**
     * Trouver l'historique par ID de formulaire, trié par date décroissante
     */
    List<FormHistory> findByFormIdOrderByCreatedAtDesc(Long formId);

    /**
     * Trouver l'historique par ID de formulaire avec pagination
     */
    Page<FormHistory> findByFormIdOrderByCreatedAtDesc(Long formId, Pageable pageable);

    /**
     * Trouver l'historique par utilisateur, trié par date décroissante
     */
    List<FormHistory> findByPerformedByIdOrderByCreatedAtDesc(Long userId);

    /**
     * Trouver l'historique par type d'action
     */
    List<FormHistory> findByActionTypeOrderByCreatedAtDesc(String actionType);

    /**
     * Trouver l'historique par secteur
     */
    List<FormHistory> findBySecteurOrderByCreatedAtDesc(String secteur);

    /**
     * Trouver l'historique dans une plage de dates
     */
    List<FormHistory> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Trouver l'historique par statut
     */
    List<FormHistory> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * Trouver l'historique des formulaires dans la bibliothèque
     */
    List<FormHistory> findByIsInLibraryTrueOrderByCreatedAtDesc();

    /**
     * Recherche globale dans l'historique
     */
    @Query("SELECT fh FROM FormHistory fh WHERE " +
            "(COALESCE(fh.formName, '') LIKE CONCAT('%', :searchTerm, '%') OR " +
            "COALESCE(fh.description, '') LIKE CONCAT('%', :searchTerm, '%') OR " +
            "COALESCE(fh.actionDescription, '') LIKE CONCAT('%', :searchTerm, '%') OR " +
            "COALESCE(fh.performedByUsername, '') LIKE CONCAT('%', :searchTerm, '%') OR " +
            "COALESCE(fh.secteur, '') LIKE CONCAT('%', :searchTerm, '%')) " +
            "ORDER BY fh.createdAt DESC")
    List<FormHistory> searchInHistory(@Param("searchTerm") String searchTerm);

    /**
     * Recherche avec filtres avancés
     */
    @Query("SELECT fh FROM FormHistory fh WHERE " +
            "(:formName IS NULL OR fh.formName LIKE CONCAT('%', :formName, '%')) AND " +
            "(:secteur IS NULL OR fh.secteur = :secteur) AND " +
            "(:actionType IS NULL OR fh.actionType = :actionType) AND " +
            "(:status IS NULL OR fh.status = :status) AND " +
            "(:performedBy IS NULL OR fh.performedByUsername LIKE CONCAT('%', :performedBy, '%')) AND " +
            "(:startDate IS NULL OR fh.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR fh.createdAt <= :endDate) " +
            "ORDER BY fh.createdAt DESC")
    List<FormHistory> findWithFilters(
            @Param("formName") String formName,
            @Param("secteur") String secteur,
            @Param("actionType") String actionType,
            @Param("status") String status,
            @Param("performedBy") String performedBy,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Statistiques par type d'action
     */
    @Query("SELECT fh.actionType, COUNT(fh) FROM FormHistory fh GROUP BY fh.actionType")
    List<Object[]> getActionTypeStatistics();

    /**
     * Statistiques par secteur
     */
    @Query("SELECT COALESCE(fh.secteur, 'Non spécifié'), COUNT(fh) FROM FormHistory fh GROUP BY fh.secteur")
    List<Object[]> getSecteurStatistics();

    /**
     * Activité récente (dernières 24h)
     */
    @Query("SELECT fh FROM FormHistory fh WHERE fh.createdAt >= :since ORDER BY fh.createdAt DESC")
    List<FormHistory> findRecentActivity(@Param("since") LocalDateTime since);

    /**
     * Compter les actions par utilisateur
     */
    @Query("SELECT fh.performedByUsername, COUNT(fh) FROM FormHistory fh " +
            "WHERE fh.performedById = :userId GROUP BY fh.performedByUsername")
    List<Object[]> countActionsByUser(@Param("userId") Long userId);


    /**
     * Recherche globale dans l'historique pour les formulaires accessibles
     */
    @Query("SELECT fh FROM FormHistory fh WHERE " +
            "fh.formId IN :accessibleFormIds AND " +
            "(COALESCE(fh.formName, '') LIKE CONCAT('%', :searchTerm, '%') OR " +
            "COALESCE(fh.description, '') LIKE CONCAT('%', :searchTerm, '%') OR " +
            "COALESCE(fh.actionDescription, '') LIKE CONCAT('%', :searchTerm, '%') OR " +
            "COALESCE(fh.performedByUsername, '') LIKE CONCAT('%', :searchTerm, '%') OR " +
            "COALESCE(fh.secteur, '') LIKE CONCAT('%', :searchTerm, '%')) " +
            "ORDER BY fh.createdAt DESC")
    List<FormHistory> searchInHistoryForAccessibleForms(
            @Param("searchTerm") String searchTerm,
            @Param("accessibleFormIds") List<Long> accessibleFormIds);

    /**
     * Recherche avec filtres avancés pour les formulaires accessibles
     */
    @Query("SELECT fh FROM FormHistory fh WHERE " +
            "fh.formId IN :accessibleFormIds AND " +
            "(:formName IS NULL OR fh.formName LIKE CONCAT('%', :formName, '%')) AND " +
            "(:secteur IS NULL OR fh.secteur = :secteur) AND " +
            "(:actionType IS NULL OR fh.actionType = :actionType) AND " +
            "(:status IS NULL OR fh.status = :status) AND " +
            "(:performedBy IS NULL OR fh.performedByUsername LIKE CONCAT('%', :performedBy, '%')) AND " +
            "(:startDate IS NULL OR fh.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR fh.createdAt <= :endDate) " +
            "ORDER BY fh.createdAt DESC")
    List<FormHistory> findWithFiltersForAccessibleForms(
            @Param("formName") String formName,
            @Param("secteur") String secteur,
            @Param("actionType") String actionType,
            @Param("status") String status,
            @Param("performedBy") String performedBy,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("accessibleFormIds") List<Long> accessibleFormIds);

    /**
     * Compter les actions pour les formulaires accessibles
     */
    /**
     * Compter les actions pour les formulaires accessibles
     */
    @Query("SELECT COUNT(fh) FROM FormHistory fh WHERE fh.formId IN :accessibleFormIds")
    Integer countByFormIdIn(@Param("accessibleFormIds") List<Long> accessibleFormIds);

    /**
     * Activité récente pour les formulaires accessibles
     */
    @Query("SELECT fh FROM FormHistory fh WHERE fh.createdAt >= :since AND fh.formId IN :accessibleFormIds ORDER BY fh.createdAt DESC")
    List<FormHistory> findRecentActivityForAccessibleForms(@Param("since") LocalDateTime since, @Param("accessibleFormIds") List<Long> accessibleFormIds);

    /**
     * Statistiques par type d'action pour les formulaires accessibles
     */
    @Query("SELECT fh.actionType, COUNT(fh) FROM FormHistory fh WHERE fh.formId IN :accessibleFormIds GROUP BY fh.actionType")
    List<Object[]> getActionTypeStatisticsForAccessibleForms(@Param("accessibleFormIds") List<Long> accessibleFormIds);

    /**
     * Statistiques par secteur pour les formulaires accessibles
     */
    @Query("SELECT COALESCE(fh.secteur, 'Non spécifié'), COUNT(fh) FROM FormHistory fh WHERE fh.formId IN :accessibleFormIds GROUP BY fh.secteur")
    List<Object[]> getSecteurStatisticsForAccessibleForms(@Param("accessibleFormIds") List<Long> accessibleFormIds);
}