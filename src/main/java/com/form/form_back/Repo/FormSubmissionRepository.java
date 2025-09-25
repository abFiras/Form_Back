package com.form.form_back.Repo;

import com.form.form_back.Entity.FormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FormSubmissionRepository extends JpaRepository<FormSubmission, Long> {
    List<FormSubmission> findByUtilisateurIdOrderBySubmittedAtDesc(Long utilisateurId);
    List<FormSubmission> findByFormIdAndStatus(Long formId, String status);

    Integer countByFormId(Long id);

    // ✅ CRITIQUE : Exclure les templates, récupérer seulement les vraies soumissions
    @Query("SELECT fs FROM FormSubmission fs " +
            "WHERE fs.formId = :formId " +
            "AND (fs.isTemplate = false OR fs.isTemplate IS NULL) " +
            "ORDER BY fs.submittedAt DESC")
    List<FormSubmission> findByFormIdAndIsTemplateFalseOrderBySubmittedAtDesc(@Param("formId") Long formId);

    // ✅ Récupérer les templates uniquement
    @Query("SELECT fs FROM FormSubmission fs " +
            "WHERE fs.formId = :formId " +
            "AND fs.isTemplate = true " +
            "ORDER BY fs.submittedAt DESC")
    List<FormSubmission> findTemplatesByFormId(@Param("formId") Long formId);

    // ✅ Soumissions par utilisateur
    @Query("SELECT fs FROM FormSubmission fs " +
            "WHERE fs.utilisateur.id = :userId " +
            "AND (fs.isTemplate = false OR fs.isTemplate IS NULL) " +
            "ORDER BY fs.submittedAt DESC")
    List<FormSubmission> findByUtilisateurIdAndIsTemplateFalse(@Param("userId") Long userId);

    // ✅ Soumissions par formulaire (toutes, y compris templates)
    List<FormSubmission> findByFormIdOrderBySubmittedAtDesc(Long formId);

    // ✅ Compter les vraies soumissions (exclut templates)
    @Query("SELECT COUNT(fs) FROM FormSubmission fs " +
            "WHERE fs.formId = :formId " +
            "AND (fs.isTemplate = false OR fs.isTemplate IS NULL)")
    Long countRealSubmissionsByFormId(@Param("formId") Long formId);

    // ✅ Soumissions récentes par utilisateur
    @Query("SELECT fs FROM FormSubmission fs " +
            "WHERE fs.utilisateur.id = :userId " +
            "AND fs.submittedAt >= :dateFrom " +
            "AND (fs.isTemplate = false OR fs.isTemplate IS NULL) " +
            "ORDER BY fs.submittedAt DESC")
    List<FormSubmission> findRecentSubmissionsByUser(@Param("userId") Long userId, @Param("dateFrom") LocalDateTime dateFrom);

    // ✅ Statistiques par statut
    @Query("SELECT fs.status, COUNT(fs) FROM FormSubmission fs " +
            "WHERE fs.formId = :formId " +
            "AND (fs.isTemplate = false OR fs.isTemplate IS NULL) " +
            "GROUP BY fs.status")
    List<Object[]> getSubmissionStatsByFormId(@Param("formId") Long formId);

    void deleteByFormIdAndIsTemplateTrue(Long id);

    List<FormSubmission> findByFormIdAndUtilisateurIdAndIsTemplateFalseOrderBySubmittedAtDesc(Long formId, Long userId);
}