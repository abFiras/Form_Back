package com.form.form_back.Repo;

import com.form.form_back.Entity.FormField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FormFieldRepository extends JpaRepository<FormField, Long> {
    List<FormField> findByFormIdOrderByOrder(Long formId);
    void deleteByFormId(Long formId);
    @Query("SELECT ff FROM FormField ff WHERE ff.form.id = :formId AND ff.type = :type")
    List<FormField> findByFormIdAndType(@Param("formId") Long formId, @Param("type") String type);

    // ✅ Compter les champs par formulaire
    @Query("SELECT COUNT(ff) FROM FormField ff WHERE ff.form.id = :formId")
    Long countByFormId(@Param("formId") Long formId);
}