package com.form.form_back.dto;

import com.form.form_back.Entity.Utilisateur;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String prenom;
    private String nom;
    private String email;
    private String phone;
    private boolean suspended;
    private boolean banned;
    private Set<String> roles;
    private GroupDTO group;  // 👈 ici on inclut un DTO, pas l'entité entière
    private String profilePhotoUrl; // ✅ AJOUT du champ photo de profil

    public UserDTO(Utilisateur user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.prenom = user.getPrenom();
        this.nom = user.getNom();
        this.phone = user.getPhone();
        this.suspended = user.getSuspended() != null ? user.getSuspended() : false;
        this.banned = user.getBanned() != null ? user.getBanned() : false;
        this.profilePhotoUrl = user.getProfilePhotoUrl(); // ✅ AJOUT

        // ✅ CORRECTION: Gestion sécurisée des rôles
        this.roles = user.getRoles() != null ?
                user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet())
                : Set.of("ROLE_USER");

        // ✅ CORRECTION: Gestion sécurisée du groupe
        this.group = user.getAssignedGroup() != null ?
                new GroupDTO(user.getAssignedGroup()) : null;
    }

    // ✅ AJOUT: Getter et Setter pour profilePhotoUrl
    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }
    public GroupDTO getGroup() {
        return group;
    }

    public void setGroup(GroupDTO group) {
        this.group = group;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }



    // getters
}
