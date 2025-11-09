package com.tasksphere.shareme.repository;

import com.tasksphere.shareme.entity.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {
    
    Optional<PasswordReset> findByTokenHashAndUsedFalse(String tokenHash);
    
    @Modifying
    @Query("DELETE FROM PasswordReset pr WHERE pr.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);
    
    @Modifying
    @Query("UPDATE PasswordReset pr SET pr.used = true WHERE pr.tokenHash = :tokenHash")
    void markTokenAsUsed(String tokenHash);
}