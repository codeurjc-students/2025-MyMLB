package com.mlb.mlbportal.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mlb.mlbportal.models.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    public Optional<UserEntity> findByUsername(String username);
}