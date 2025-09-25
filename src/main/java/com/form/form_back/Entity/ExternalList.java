package com.form.form_back.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "external_lists")
public class ExternalList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "list_type", nullable = false)
    private String listType; // "STATIC" ou "DYNAMIC"

    @Column(name = "rubrique")
    private String rubrique; // Catégorie de la liste

    @Column(name = "is_advanced", nullable = false)
    private Boolean isAdvanced = false;

    @Column(name = "is_filtered", nullable = false)
    private Boolean isFiltered = false;


    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Utilisateur utilisateur;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "externalList", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ExternalListItem> items;

    // Constructeurs
    public ExternalList() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public ExternalList(String name, String description, String listType) {
        this();
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

    public Boolean getAdvanced() {
        return isAdvanced;
    }

    public void setAdvanced(Boolean advanced) {
        isAdvanced = advanced;
    }

    public Boolean getFiltered() {
        return isFiltered;
    }

    public void setFiltered(Boolean filtered) {
        isFiltered = filtered;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
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

    public List<ExternalListItem> getItems() {
        return items;
    }

    public void setItems(List<ExternalListItem> items) {
        this.items = items;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}