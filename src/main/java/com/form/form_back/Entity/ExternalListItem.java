package com.form.form_back.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "external_list_items")
public class ExternalListItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String value;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Colonnes additionnelles pour des données étendues
    @Column(name = "extra_data", columnDefinition = "TEXT")
    private String extraData; // JSON pour données supplémentaires

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "external_list_id", nullable = false)
    private ExternalList externalList;

    // Constructeurs
    public ExternalListItem() {}

    public ExternalListItem(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public ExternalListItem(String label, String value, ExternalList externalList) {
        this.label = label;
        this.value = value;
        this.externalList = externalList;
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

    public ExternalList getExternalList() {
        return externalList;
    }

    public void setExternalList(ExternalList externalList) {
        this.externalList = externalList;
    }
}