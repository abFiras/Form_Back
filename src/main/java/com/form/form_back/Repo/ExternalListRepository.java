package com.form.form_back.Repo;

import com.form.form_back.Entity.ExternalList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExternalListRepository extends JpaRepository<ExternalList, Long> {

    /**
     * Trouve une liste externe avec ses éléments
     */
    @Query("SELECT el FROM ExternalList el LEFT JOIN FETCH el.items WHERE el.id = :id")
    Optional<ExternalList> findByIdWithItems(@Param("id") Long id);

    /**
     * Trouve toutes les listes externes créées par un utilisateur
     */
    List<ExternalList> findByCreatedByOrderByCreatedAtDesc(Long createdBy);

    /**
     * Trouve les listes externes par rubrique
     */
    List<ExternalList> findByRubriqueOrderByNameAsc(String rubrique);

    /**
     * Trouve les listes externes par nom (recherche insensible à la casse)
     */
    List<ExternalList> findByNameContainingIgnoreCaseOrderByNameAsc(String name);

    /**
     * Trouve toutes les rubriques distinctes
     */
    @Query("SELECT DISTINCT el.rubrique FROM ExternalList el WHERE el.rubrique IS NOT NULL ORDER BY el.rubrique")
    List<String> findDistinctRubriques();

    /**
     * Compte le nombre d'éléments dans une liste externe
     */
    @Query("SELECT COUNT(eli) FROM ExternalListItem eli WHERE eli.externalList.id = :listId AND eli.isActive = true")
    Long countActiveItemsByListId(@Param("listId") Long listId);

    /**
     * Trouve les listes externes par type
     */
    List<ExternalList> findByListTypeOrderByNameAsc(String listType);

    /**
     * Trouve les listes externes avancées
     */
    List<ExternalList> findByIsAdvancedTrueOrderByNameAsc();

    /**
     * Trouve les listes externes filtrées
     */
    List<ExternalList> findByIsFilteredTrueOrderByNameAsc();
}