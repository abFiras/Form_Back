package com.form.form_back.Repo;

import com.form.form_back.Entity.FormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FormSubmissionRepository extends JpaRepository<FormSubmission, Long> {
    List<FormSubmission> findByFormIdOrderBySubmittedAtDesc(Long formId);
}