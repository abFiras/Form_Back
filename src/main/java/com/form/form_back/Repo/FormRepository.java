package com.form.form_back.Repo;

import com.form.form_back.Entity.Form;
import com.form.form_back.Entity.FormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormRepository extends JpaRepository<Form, Long> {
    List<Form> findByStatusOrderByCreatedAtDesc(String status);

    @Query("SELECT f FROM Form f LEFT JOIN FETCH f.fields WHERE f.id = :id")
    Form findByIdWithFields(Long id);
}