package com.form.form_back.Repo;

import com.form.form_back.Entity.LibraryForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LibraryFormRepository extends JpaRepository<LibraryForm, Long> {

    /**
     * Recherche par nom ou description
     */
    List<LibraryForm> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String description);

    /**
     * Filtrer par origine
     */
    List<LibraryForm> findByOriginAndIsActiveTrue(String origin);

    /**
     * Filtrer par langue
     */
    List<LibraryForm> findByLanguageAndIsActiveTrue(String language);

    /**
     * Recherche combinée avec filtres
     */
    @Query("SELECT lf FROM LibraryForm lf WHERE lf.isActive = true " +
            "AND (:search IS NULL OR :search = '' OR " +
            "LOWER(lf.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(lf.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(lf.tags) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:origin IS NULL OR :origin = '' OR lf.origin = :origin) " +
            "AND (:language IS NULL OR :language = '' OR lf.language = :language)")
    List<LibraryForm> findLibraryFormsWithFilters(
            @Param("search") String search,
            @Param("origin") String origin,
            @Param("language") String language);

    /**
     * Formulaires les plus populaires
     */
    @Query("SELECT lf FROM LibraryForm lf WHERE lf.isActive = true " +
            "ORDER BY (lf.viewCount + lf.downloadCount) DESC")
    List<LibraryForm> findTopByOrderByViewCountDescDownloadCountDesc();

    /**
     * Formulaires les plus récents
     */
    @Query("SELECT lf FROM LibraryForm lf WHERE lf.isActive = true " +
            "ORDER BY lf.createdAt DESC")
    List<LibraryForm> findTopByOrderByCreatedAtDesc();

    /**
     * Formulaires récemment mis à jour
     */
    @Query("SELECT lf FROM LibraryForm lf WHERE lf.isActive = true " +
            "ORDER BY lf.updatedAt DESC")
    List<LibraryForm> findTopByOrderByUpdatedAtDesc();

    /**
     * Formulaires par utilisateur
     */
    List<LibraryForm> findBySharedByAndIsActiveTrue(String sharedBy);

    /**
     * Compte le nombre de formulaires dans la bibliothèque
     */
    long countByIsActiveTrue();

    /**
     * Récupère les tags les plus utilisés
     */
    @Query("SELECT lf.tags FROM LibraryForm lf WHERE lf.isActive = true AND lf.tags IS NOT NULL")
    List<String> findAllTags();
}