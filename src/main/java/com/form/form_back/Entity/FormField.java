package com.form.form_back.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "form_fields")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormField {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    @JsonBackReference
    private Form form;

    @Column(nullable = true)
    private String type; // TEXT, EMAIL, NUMBER, DATE, SELECT, CHECKBOX, RADIO, TEXTAREA, FILE

    @Column(nullable = false)
    private String label;
    @Column(name = "field_name", nullable = true) // ✅ Ajouté fieldName
    private String fieldName;

    private String placeholder;

    @Column(name = "`order`", nullable = true) // ✅ autoriser null
    private Integer order;


    @Column(nullable = false)
    private Boolean required = false;

    @Column(columnDefinition = "TEXT")
    private String options; // JSON pour les options (select, radio, checkbox)

    @Column(columnDefinition = "TEXT")
    private String validation; // JSON pour les règles de validation

    @Column(columnDefinition = "TEXT")
    private String styling; // JSON pour le style CSS
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "external_list_id")
    private ExternalList externalList;

    // ✅ NOUVEAU: Indique si le champ utilise une liste externe
    @Column(name = "use_external_list")
    private Boolean useExternalList = false;


    public ExternalList getExternalList() {
        return externalList;
    }

    public void setExternalList(ExternalList externalList) {
        this.externalList = externalList;
    }

    public Boolean getUseExternalList() {
        return useExternalList;
    }

    public void setUseExternalList(Boolean useExternalList) {
        this.useExternalList = useExternalList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
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

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
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