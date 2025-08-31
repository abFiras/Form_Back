package com.form.form_back.dto;

import java.util.List;

// DTO pour la création d'une nouvelle liste externe
public class CreateExternalListRequest {
    private String name;
    private String description;
    private String listType;
    private String rubrique;
    private Boolean isAdvanced = false;
    private Boolean isFiltered = false;
    private List<ExternalListItemDTO> items;

    // Getters et Setters
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

    public String getListType() {
        return listType;
    }

    public void setListType(String listType) {
        this.listType = listType;
    }

    public String getRubrique() {
        return rubrique;
    }

    public void setRubrique(String rubrique) {
        this.rubrique = rubrique;
    }

    public Boolean getIsAdvanced() {
        return isAdvanced;
    }

    public void setIsAdvanced(Boolean isAdvanced) {
        this.isAdvanced = isAdvanced;
    }

    public Boolean getIsFiltered() {
        return isFiltered;
    }

    public void setIsFiltered(Boolean isFiltered) {
        this.isFiltered = isFiltered;
    }

    public List<ExternalListItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ExternalListItemDTO> items) {
        this.items = items;
    }
}