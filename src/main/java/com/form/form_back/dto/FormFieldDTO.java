package com.form.form_back.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormFieldDTO {
    private Long id;
    private String type;
    private String label;
    private String fieldName;
    private String placeholder;
    private Integer order;
    private Boolean required;
    private List<FieldOptionDTO> options; // ✅ Changé de String vers List<FieldOptionDTO>
    private String validation; // JSON string
    private String styling; // JSON string

    // ✅ NOUVEAU: Champ pour les attributs (pour liste externe, calculs, etc.)
    private Map<String, Object> attributes;

    // ✅ NOUVEAU: Propriétés spécifiques pour liste externe (pour compatibilité frontend)
    private Long externalListId;
    private String externalListDisplayMode;
    private String externalListUrl;
    private Map<String, Object> externalListParams;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Long getExternalListId() {
        return externalListId;
    }

    public void setExternalListId(Long externalListId) {
        this.externalListId = externalListId;
    }

    public String getExternalListDisplayMode() {
        return externalListDisplayMode;
    }

    public void setExternalListDisplayMode(String externalListDisplayMode) {
        this.externalListDisplayMode = externalListDisplayMode;
    }

    public String getExternalListUrl() {
        return externalListUrl;
    }

    public void setExternalListUrl(String externalListUrl) {
        this.externalListUrl = externalListUrl;
    }

    public Map<String, Object> getExternalListParams() {
        return externalListParams;
    }

    public void setExternalListParams(Map<String, Object> externalListParams) {
        this.externalListParams = externalListParams;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }


    public List<FieldOptionDTO> getOptions() {
        return options;
    }

    public void setOptions(List<FieldOptionDTO> options) {
        this.options = options;
    }

    public String getValidation() {
        return validation;
    }

    public void setValidation(String validation) {
        this.validation = validation;
    }

    public String getStyling() {
        return styling;
    }

    public void setStyling(String styling) {
        this.styling = styling;
    }
}
