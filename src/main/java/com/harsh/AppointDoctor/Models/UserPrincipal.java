package com.harsh.AppointDoctor.Models;

import com.harsh.AppointDoctor.Enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {

    private final Users user;

    public UserPrincipal(Users user){
        this.user= user;
    }

    public boolean isDoctor() {
        return user.getRoles().contains(Role.DOCTOR);
    }

    public boolean isUser(){
        return  user.getRoles().contains(Role.USER);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();
    }


    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }
}
