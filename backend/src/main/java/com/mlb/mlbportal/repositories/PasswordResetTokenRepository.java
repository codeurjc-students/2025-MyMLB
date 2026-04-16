package com.mlb.mlbportal.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mlb.mlbportal.models.PasswordResetToken;
import com.mlb.mlbportal.models.UserEntity;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    @EntityGraph(attributePaths = {"user"})
    Optional<PasswordResetToken> findByCode(String code);

    @EntityGraph(attributePaths = {"user"})
    Optional<PasswordResetToken> findByUser(UserEntity user);
}