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
    public void UpdateROle(Long id, String role) {

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
