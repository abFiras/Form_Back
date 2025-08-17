package com.form.form_back.Repo;

import com.form.form_back.Entity.ERole;
import com.form.form_back.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
    boolean existsByName(ERole r1);
}