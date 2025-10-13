package com.mlb.mlbportal.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.repositories.UserRepository;

@Service
public class SecurityUserDetails implements UserDetailsService {
    private final  UserRepository userRepository;

    public SecurityUserDetails(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        // Get the user
        UserEntity user = this.userRepository.findByUsername(username).orElseThrow( () -> new UsernameNotFoundException("User not found"));

        // Add the user roles in the way Spring recognize them
        List<GrantedAuthority> roles = new ArrayList<>();
        for (String role: user.getRoles()) {
            roles.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
        return new UserPrincipal(user, roles);
    }   
}