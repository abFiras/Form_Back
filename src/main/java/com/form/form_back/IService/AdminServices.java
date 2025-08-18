package com.form.form_back.IService;

import com.form.form_back.Entity.Role;
import com.form.form_back.Entity.Utilisateur;

import java.util.List;

public interface  AdminServices {
    List<Utilisateur> getall();
     Utilisateur updateUser(Long id, Utilisateur updatedUser) ;
    List<Role> getAllROles();
    String banUser(String email);
    String suspendUser(String email);
    String unbanUser(String email);
    String automaticUnbanUser();
    void deleteUser(String email);
}
