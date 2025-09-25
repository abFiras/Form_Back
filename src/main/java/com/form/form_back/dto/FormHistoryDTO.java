package com.form.form_back.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormHistoryDTO {
    private Long id;
    private Long formId;
    private String formName;
    private String secteur;
    private String description;
    private String status;
    private String statusLabel;
    private String actionType;
    private String actionTypeLabel;
    private String actionDescription;
    private Long performedById;
    private String performedByUsername;
    private String performedByEmail;
    private List<GroupInfoDTO> assignedGroups;
    private Integer fieldCount;
    private Boolean isInLibrary;
    private LocalDateTime librarySharedDate;
    private String changesDetails;
    private String ipAddress;
    private LocalDateTime createdAt;

    // Pour l'affichage formaté
    private String timeAgo;
    private String formattedDate;
    private String actionIcon;
    private String actionColor;

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

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionTypeLabel() {
        return actionTypeLabel;
    }

    public void setActionTypeLabel(String actionTypeLabel) {
        this.actionTypeLabel = actionTypeLabel;
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

    public List<GroupInfoDTO> getAssignedGroups() {
        return assignedGroups;
    }

    public void setAssignedGroups(List<GroupInfoDTO> assignedGroups) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public void setFormattedDate(String formattedDate) {
        this.formattedDate = formattedDate;
    }

    public String getActionIcon() {
        return actionIcon;
    }

    public void setActionIcon(String actionIcon) {
        this.actionIcon = actionIcon;
    }

    public String getActionColor() {
        return actionColor;
    }

    public void setActionColor(String actionColor) {
        this.actionColor = actionColor;
    }
}
