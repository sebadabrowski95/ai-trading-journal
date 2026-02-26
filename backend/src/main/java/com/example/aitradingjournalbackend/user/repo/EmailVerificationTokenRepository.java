package com.example.aitradingjournalbackend.user.repo;

import com.example.aitradingjournalbackend.user.AppUser;
import com.example.aitradingjournalbackend.user.EmailVerificationToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);

    Optional<EmailVerificationToken> findFirstByUserEmailIgnoreCaseOrderByIdDesc(String email);

    void deleteByUser(AppUser user);
}
