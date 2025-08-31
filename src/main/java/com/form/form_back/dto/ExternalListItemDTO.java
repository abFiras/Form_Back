package com.form.form_back.dto;

import java.util.List;

public class ExternalListItemDTO {
    private Long id;
    private String label;
    private String value;
    private Integer displayOrder;
    private Boolean isActive;
    private String extraData;

    // Constructeurs
    public ExternalListItemDTO() {}

    public ExternalListItemDTO(String label, String value) {
        this.label = label;
        this.value = value;
        this.isActive = true;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }
}


