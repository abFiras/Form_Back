package com.form.form_back.Service;

import com.form.form_back.Entity.Role;
import com.form.form_back.Entity.Utilisateur;
import com.form.form_back.IService.AdminServices;
import com.form.form_back.Repo.RoleRepository;
import com.form.form_back.Repo.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

@Service
public class AdminServicesImpl implements AdminServices {
    @Autowired
    UtilisateurRepository userRepository;
    @Autowired
    RoleRepository roleRepository;

    @Override
    public List<Utilisateur> getall() {
        return userRepository.findAll();
    }

    @Override
    public Utilisateur updateUser(Long id, Utilisateur updatedUser) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setUsername(updatedUser.getUsername());
                    user.setEmail(updatedUser.getEmail());
                    user.setPhone(updatedUser.getPhone());
                    user.setPrenom(updatedUser.getPrenom());
                    user.setNom(updatedUser.getNom());
                    user.setSuspended(updatedUser.getSuspended());

                    // ✅ FIX: Gestion correcte des rôles
                    if (updatedUser.getRoles() != null && !updatedUser.getRoles().isEmpty()) {
                        Set<Role> roles = new HashSet<>();
                        for (Role role : updatedUser.getRoles()) {
                            // Si le rôle contient juste un nom, le chercher dans la DB
                            if (role.getId() == null) {
                                Optional<Role> dbRole = roleRepository.findByName(role.getName());
                                if (dbRole.isPresent()) {
                                    roles.add(dbRole.get());
                                }
                            } else {
                                // Si le rôle a un ID, le chercher par ID
                                Optional<Role> dbRole = roleRepository.findById(role.getId());
                                if (dbRole.isPresent()) {
                                    roles.add(dbRole.get());
                                }
                            }
                        }
                        user.setRoles(roles);
                    }

                    // Ne pas modifier le mot de passe si il n'est pas fourni
                    if (updatedUser.getPassword() != null && !updatedUser.getPassword().trim().isEmpty()) {
                        user.setPassword(updatedUser.getPassword());
                    }

                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    @Override
    public List<Role> getAllROles() {
        return roleRepository.findAll();
    }

    @Override
    public String banUser(String email) {
        Optional<Utilisateur> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            Utilisateur utilisateur = user.get();
            utilisateur.setBanned(true);
            userRepository.save(utilisateur);
            return "User banned successfully.";
        } else {
            return "User not found.";
        }
    }

    @Override
    public String suspendUser(String email) {
        Optional<Utilisateur> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            Utilisateur utilisateur = user.get();
            utilisateur.setSuspended(true);
            userRepository.save(utilisateur);
            return "User suspended successfully.";
        } else {
            return "User not found.";
        }
    }

    @Override
    public String unbanUser(String email) {
        Optional<Utilisateur> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            Utilisateur utilisateur = user.get();
            utilisateur.setBanned(false);
            userRepository.save(utilisateur);
            return "User unbanned successfully.";
        } else {
            return "User not found.";
        }
    }

    @Override
    public String automaticUnbanUser() {
        List<Utilisateur> bannedUsers = userRepository.findAllByBanned(true);
        for (Utilisateur user : bannedUsers) {
            if (user.getBanEndDate() != null && user.getBanEndDate().before(new Date())) {
                user.setBanned(false);
                userRepository.save(user);
            }
        }
        return "Automatic unban completed.";
    }

    @Override
    public void deleteUser(String email) {
        Optional<Utilisateur> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            userRepository.delete(user.get());
        }
    }
}