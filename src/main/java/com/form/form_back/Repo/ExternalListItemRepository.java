package com.form.form_back.Repo;

import com.form.form_back.Entity.ExternalListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExternalListItemRepository extends JpaRepository<ExternalListItem, Long> {

    /**
     * Trouve tous les éléments actifs d'une liste externe, triés par ordre d'affichage
     */
    List<ExternalListItem> findByExternalListIdAndIsActiveTrueOrderByDisplayOrderAsc(Long externalListId);

    /**
     * Trouve tous les éléments d'une liste externe (actifs et inactifs)
     */
    List<ExternalListItem> findByExternalListIdOrderByDisplayOrderAsc(Long externalListId);

    /**
     * Supprime tous les éléments d'une liste externe
     */
    @Modifying
    @Query("DELETE FROM ExternalListItem eli WHERE eli.externalList.id = :listId")
    void deleteByExternalListId(@Param("listId") Long listId);

    /**
     * Désactive tous les éléments d'une liste externe
     */
    @Modifying
    @Query("UPDATE ExternalListItem eli SET eli.isActive = false WHERE eli.externalList.id = :listId")
    void deactivateAllByExternalListId(@Param("listId") Long listId);

    /**
     * Trouve les éléments par valeur
     */
    List<ExternalListItem> findByValueAndExternalListId(String value, Long externalListId);

    /**
     * Recherche des éléments par label (insensible à la casse)
     */
    List<ExternalListItem> findByLabelContainingIgnoreCaseAndExternalListIdOrderByDisplayOrderAsc(
            String label, Long externalListId);

    /**
     * Compte les éléments actifs d'une liste
     */
    @Query("SELECT COUNT(eli) FROM ExternalListItem eli WHERE eli.externalList.id = :listId AND eli.isActive = true")
    Long countActiveItemsByListId(@Param("listId") Long listId);

    /**
     * Met à jour l'ordre d'affichage d'un élément
     */
    @Modifying
    @Query("UPDATE ExternalListItem eli SET eli.displayOrder = :order WHERE eli.id = :id")
    void updateDisplayOrder(@Param("id") Long id, @Param("order") Integer order);
}