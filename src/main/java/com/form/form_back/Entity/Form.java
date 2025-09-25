package com.form.form_back.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

@Entity
@Table(name = "forms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Form {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String secteur;

    private String description;

    @Column(nullable = true)
    private String status = "DRAFT"; // DRAFT, PUBLISHED, ARCHIVED

    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<FormField> fields = new ArrayList<>();

    // ✅ NOUVEAU : Groupes assignés au formulaire (Many-to-Many)
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "form_groups",
            joinColumns = @JoinColumn(name = "form_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id"))
    @JsonIgnoreProperties({"users", "hibernateLazyInitializer", "handler"})
    private Set<Group> assignedGroups = new HashSet<>();
    // NOUVEAU: Créateur du formulaire
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")

    private Utilisateur createdBy;
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Set<Group> getAssignedGroups() {
        return assignedGroups;
    }

    public void setAssignedGroups(Set<Group> assignedGroups) {
        this.assignedGroups = assignedGroups;
    }

    // ✅ Méthodes utilitaires pour gérer les groupes
    public void addGroup(Group group) {
        this.assignedGroups.add(group);
    }

    public void removeGroup(Group group) {
        this.assignedGroups.remove(group);
    }

    public boolean isAccessibleByUser(Utilisateur user) {
        if (assignedGroups.isEmpty()) {
            return true; // Si aucun groupe assigné, accessible à tous
        }

        // Vérifier si l'utilisateur appartient à au moins un des groupes assignés
        return assignedGroups.stream()
                .anyMatch(group -> user.getGroups().contains(group) ||
                        (user.getAssignedGroup() != null && user.getAssignedGroup().equals(group)));
    }

    public String getSecteur() {
        return secteur;
    }

    public void setSecteur(String secteur) {
        this.secteur = secteur;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<FormField> getFields() {
        return fields;
    }

    public void setFields(List<FormField> fields) {
        this.fields = fields;
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


    public Utilisateur getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Utilisateur createdBy) {
        this.createdBy = createdBy;
    }
}
