package com.form.form_back.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "groups_ds")
public class Group implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column
    private String logoUrl;

    @Column
    private String color; // Couleur associée au groupe

    @Column
    private Boolean active = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Set<Form> getForms() {
        return forms;
    }

    public void setForms(Set<Form> forms) {
        this.forms = forms;
    }

    @ManyToMany(mappedBy = "groups", fetch = FetchType.LAZY)
    @JsonIgnore // Ignorer complètement pour éviter les références circulaires
    private Set<Utilisateur> users = new HashSet<>();

    // Utilisateurs ayant ce groupe comme groupe assigné principal
    @OneToMany(mappedBy = "assignedGroup", fetch = FetchType.LAZY)
    @JsonIgnore // Ignorer pour éviter les références circulaires
    private Set<Utilisateur> assignedUsers = new HashSet<>();
    // Constructeurs

    public Group(String name, String description, String logoUrl, String color) {
        this.name = name;
        this.description = description;
        this.logoUrl = logoUrl;
        this.color = color;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Set<Utilisateur> getUsers() { return users; }
    public void setUsers(Set<Utilisateur> users) { this.users = users; }
    // ✅ NOUVEAU : Relation avec les formulaires
    @ManyToMany(mappedBy = "assignedGroups", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"assignedGroups", "hibernateLazyInitializer", "handler"})
    private Set<Form> forms = new HashSet<>();
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Group)) return false;
        Group group = (Group) o;
        return Objects.equals(id, group.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", active=" + active +
                '}';
    }
    public Group() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    public Group(String name) {
        this();
        this.name = name;
    }

    public Group(String name, String description) {
        this(name);
        this.description = description;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Méthodes utilitaires
    public void addUser(Utilisateur user) {
        this.users.add(user);
        user.getGroups().add(this);
    }

    public void removeUser(Utilisateur user) {
        this.users.remove(user);
        user.getGroups().remove(this);
    }
    public Set<Utilisateur> getAssignedUsers() {
        return assignedUsers;
    }

    public void setAssignedUsers(Set<Utilisateur> assignedUsers) {
        this.assignedUsers = assignedUsers;
    }
}