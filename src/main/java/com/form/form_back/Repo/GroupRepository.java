package com.form.form_back.Repo;

import com.form.form_back.Entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByName(String name);
    List<Group> findByActiveTrue();

    @Query("SELECT g FROM Group g ORDER BY g.id")
    List<Group> findAllOrderedById();

    // ✅ NOUVEAU : Compter les utilisateurs par groupe
    @Query("SELECT COUNT(u) FROM Utilisateur u JOIN u.groups g WHERE g = :group")
    Long countUsersByGroup(@Param("group") Group group);

    // ✅ NOUVEAU : Groupes avec leurs formulaires
    @Query("SELECT DISTINCT g FROM Group g " +
            "LEFT JOIN FETCH g.forms " +
            "WHERE g.active = true")
    List<Group> findActiveGroupsWithForms();
}