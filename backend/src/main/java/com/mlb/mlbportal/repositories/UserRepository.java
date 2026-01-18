package com.mlb.mlbportal.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mlb.mlbportal.handler.notFound.UserNotFoundException;
import com.mlb.mlbportal.models.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);

    default UserEntity findByUsernameOrThrow(String username) {
        return this.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User Not Found"));
    }
    
    Optional<UserEntity> findByEmail(String email);
}