package com.form.form_back.Repo;

import com.form.form_back.Entity.FormField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FormFieldRepository extends JpaRepository<FormField, Long> {
    List<FormField> findByFormIdOrderByOrder(Long formId);
    void deleteByFormId(Long formId);
}