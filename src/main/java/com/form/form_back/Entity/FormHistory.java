package com.form.form_back.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "form_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "form_id", nullable = false)
    private Long formId;

    @Column(name = "form_name", nullable = false)
    private String formName;

    @Column(name = "secteur")
    private String secteur;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status")
    private String status;

    @Column(name = "action_type", nullable = false)
    private String actionType; // CREATED, UPDATED, PUBLISHED, ARCHIVED, DELETED, SHARED_TO_LIBRARY, REMOVED_FROM_LIBRARY

    @Column(name = "action_description", columnDefinition = "TEXT")
    private String actionDescription;

    @Column(name = "performed_by_id")
    private Long performedById;

    @Column(name = "performed_by_username")
    private String performedByUsername;

    @Column(name = "performed_by_email")
    private String performedByEmail;

    @Column(name = "assigned_groups", columnDefinition = "TEXT")
    private String assignedGroups; // JSON format: [{"id":1,"name":"Group1"},...]

    @Column(name = "field_count")
    private Integer fieldCount;

    @Column(name = "is_in_library")
    private Boolean isInLibrary = false;

    @Column(name = "library_shared_date")
    private LocalDateTime librarySharedDate;

    @Column(name = "changes_details", columnDefinition = "TEXT")
    private String changesDetails; // JSON format pour les détails des changements

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Méthodes utilitaires
    public String getActionTypeLabel() {
        switch (actionType) {
            case "CREATED": return "Création";
            case "UPDATED": return "Modification";
            case "PUBLISHED": return "Publication";
            case "ARCHIVED": return "Archivage";
            case "DELETED": return "Suppression";
            case "SHARED_TO_LIBRARY": return "Partagé vers bibliothèque";
            case "REMOVED_FROM_LIBRARY": return "Retiré de la bibliothèque";
            case "GROUPS_ASSIGNED": return "Groupes assignés";
            case "GROUPS_UPDATED": return "Groupes modifiés";
            case "STATUS_CHANGED": return "Statut modifié";
            case "FIELDS_UPDATED": return "Champs modifiés";
            default: return actionType;
        }
    }

    public String getStatusLabel() {
        switch (status) {
            case "DRAFT": return "Brouillon";
            case "PUBLISHED": return "Publié";
            case "ARCHIVED": return "Archivé";
            case "DELETED": return "Supprimé";
            default: return status != null ? status : "Inconnu";
        }
    }

    public Long getId() {
        return id;
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

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public String getSecteur() {
        return secteur;
    }

    public void setSecteur(String secteur) {
        this.secteur = secteur;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionDescription() {
        return actionDescription;
    }

    public void setActionDescription(String actionDescription) {
        this.actionDescription = actionDescription;
    }

    public Long getPerformedById() {
        return performedById;
    }

    public void setPerformedById(Long performedById) {
        this.performedById = performedById;
    }

    public String getPerformedByUsername() {
        return performedByUsername;
    }

    public void setPerformedByUsername(String performedByUsername) {
        this.performedByUsername = performedByUsername;
    }

    public String getPerformedByEmail() {
        return performedByEmail;
    }

    public void setPerformedByEmail(String performedByEmail) {
        this.performedByEmail = performedByEmail;
    }

    public String getAssignedGroups() {
        return assignedGroups;
    }

    public void setAssignedGroups(String assignedGroups) {
        this.assignedGroups = assignedGroups;
    }

    public Integer getFieldCount() {
        return fieldCount;
    }

    public void setFieldCount(Integer fieldCount) {
        this.fieldCount = fieldCount;
    }

    public Boolean getIsInLibrary() {
        return isInLibrary;
    }

    public void setIsInLibrary(Boolean inLibrary) {
        isInLibrary = inLibrary;
    }

    public LocalDateTime getLibrarySharedDate() {
        return librarySharedDate;
    }

    public void setLibrarySharedDate(LocalDateTime librarySharedDate) {
        this.librarySharedDate = librarySharedDate;
    }

    public String getChangesDetails() {
        return changesDetails;
    }

    public void setChangesDetails(String changesDetails) {
        this.changesDetails = changesDetails;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}