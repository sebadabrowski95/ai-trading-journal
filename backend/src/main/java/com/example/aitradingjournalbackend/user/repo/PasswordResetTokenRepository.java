package com.example.aitradingjournalbackend.user.repo;

import com.example.aitradingjournalbackend.user.AppUser;
import com.example.aitradingjournalbackend.user.PasswordResetToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUser(AppUser user);
}
