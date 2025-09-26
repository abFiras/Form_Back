package com.form.form_back.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "form_submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "form_id", nullable = false)
    private Long formId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String data; // JSON contenant toutes les réponses
    @Column(name = "status")
    private String status = "SUBMITTED"; // SUBMITTED, REVIEWED, APPROVED, REJECTED

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt = LocalDateTime.now();
    // ✅ NOUVEAU : Indicateur pour savoir si c'est une copie ou l'original
    @Column(name = "is_template", nullable = false)
    private Boolean isTemplate = false; // false = soumission réelle, true = template vide

    // ✅ NOUVEAU : Référence vers le formulaire template original
    @Column(name = "template_id")
    private Long templateId;
    private String submitterEmail;
    private String submitterIp;

    public Boolean getIsTemplate() {
        return isTemplate;
    }

    public void setIsTemplate(Boolean template) {
        isTemplate = template;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Long getId() {
        return id;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFormId() {
        return formId;
    }

    public void setFormId(Long formId) {
        this.formId = formId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getSubmitterEmail() {
        return submitterEmail;
    }

    public void setSubmitterEmail(String submitterEmail) {
        this.submitterEmail = submitterEmail;
    }

    public String getSubmitterIp() {
        return submitterIp;
    }

    public void setSubmitterIp(String submitterIp) {
        this.submitterIp = submitterIp;
    }
}
