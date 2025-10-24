package com.mlb.mlbportal.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.mlb.mlbportal.models.UserEntity;

import lombok.Getter;

@Getter
public class UserPrincipal implements UserDetails {
    private final transient UserEntity user;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(UserEntity user, Collection<? extends  GrantedAuthority> authorities) {
        this.user = user;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.user.getPassword();
    }

    @Override
    public String getUsername() {
        return this.user.getUsername();
    }   
}