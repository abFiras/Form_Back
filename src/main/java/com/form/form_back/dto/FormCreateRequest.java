package com.form.form_back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormCreateRequest {
    @NotBlank(message = "Le nom du formulaire est obligatoire")
    private String name;
    private String secteur;
    private String description;

    @NotNull(message = "L'ID utilisateur est obligatoire")
    private Long userId;

    private List<FormFieldCreateDTO> fields = new ArrayList<>();

    // ✅ NOUVEAU : Liste des IDs des groupes assignés
    private List<Long> groupIds = new ArrayList<>();

    public String getSecteur() {
        return secteur;
    }

    public void setSecteur(String secteur) {
        this.secteur = secteur;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<FormFieldCreateDTO> getFields() {
        return fields;
    }

    public void setFields(List<FormFieldCreateDTO> fields) {
        this.fields = fields;
    }

    public List<Long> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(List<Long> groupIds) {
        this.groupIds = groupIds;
    }
}