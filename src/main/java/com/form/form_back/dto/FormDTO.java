package com.form.form_back.dto;


import jakarta.persistence.Column;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormDTO {
    private Long id;
    private String name;
    private String description;
    private String status;
    private List<FormFieldDTO> fields = new ArrayList<>();

    private LocalDateTime createdAt ;

    private LocalDateTime updatedAt;
    private Long createdBy; // ID du créateur

    // ✅ NOUVEAU : Informations des groupes assignés
    private List<Long> assignedGroupIds = new ArrayList<>();
    private List<GroupDTO> assignedGroups = new ArrayList<>();

    // ✅ NOUVEAU : Indicateurs d'accès
    private Boolean isAccessible; // Si l'utilisateur actuel peut y accéder
    private Boolean canEdit; // Si l'utilisateur actuel peut l'éditer

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public List<Long> getAssignedGroupIds() {
        return assignedGroupIds;
    }

    public void setAssignedGroupIds(List<Long> assignedGroupIds) {
        this.assignedGroupIds = assignedGroupIds;
    }

    public List<GroupDTO> getAssignedGroups() {
        return assignedGroups;
    }

    public void setAssignedGroups(List<GroupDTO> assignedGroups) {
        this.assignedGroups = assignedGroups;
    }

    public Boolean getIsAccessible() {
        return isAccessible;
    }

    public void setIsAccessible(Boolean accessible) {
        isAccessible = accessible;
    }

    public Boolean getCanEdit() {
        return canEdit;
    }

    public void setCanEdit(Boolean canEdit) {
        this.canEdit = canEdit;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<FormFieldDTO> getFields() {
        return fields;
    }

    public void setFields(List<FormFieldDTO> fields) {
        this.fields = fields;
    }
}
