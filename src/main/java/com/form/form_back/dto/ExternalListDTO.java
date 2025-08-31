package com.form.form_back.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ExternalListDTO {
    private Long id;
    private String name;
    private String description;
    private String listType;
    private String rubrique;
    private Boolean isAdvanced;
    private Boolean isFiltered;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer itemCount;
    private List<ExternalListItemDTO> items;

    // Constructeurs
    public ExternalListDTO() {}

    public ExternalListDTO(String name, String description, String listType) {
        this.name = name;
        this.description = description;
        this.listType = listType;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
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

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }

    public List<ExternalListItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ExternalListItemDTO> items) {
        this.items = items;
    }
}