package com.example.aitradingjournalbackend.auth;

import com.example.aitradingjournalbackend.auth.dto.PasswordResetConfirmRequest;
import com.example.aitradingjournalbackend.auth.dto.PasswordResetRequest;
import com.example.aitradingjournalbackend.auth.dto.RegisterRequest;
import com.example.aitradingjournalbackend.user.AppUser;
import com.example.aitradingjournalbackend.user.EmailVerificationToken;
import com.example.aitradingjournalbackend.user.PasswordResetToken;
import com.example.aitradingjournalbackend.user.repo.AppUserRepository;
import com.example.aitradingjournalbackend.user.repo.EmailVerificationTokenRepository;
import com.example.aitradingjournalbackend.user.repo.PasswordResetTokenRepository;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivationEmailService activationEmailService;
    private final ActivationProperties activationProperties;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetEmailService passwordResetEmailService;
    private final PasswordResetProperties passwordResetProperties;

    @Transactional
    public void register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        if (appUserRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            return;
        }

        AppUser user = appUserRepository.save(new AppUser(
            normalizedEmail,
            passwordEncoder.encode(request.password()),
            false
        ));

        tokenRepository.deleteByUser(user);
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusSeconds(activationProperties.tokenExpirationHours() * 3600);
        tokenRepository.save(new EmailVerificationToken(token, user, expiresAt));

        activationEmailService.sendActivationEmail(normalizedEmail, token);
    }

    @Transactional
    public void activateAccount(String token) {
        if (!StringUtils.hasText(token)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid activation token");
        }

        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid activation token"));

        if (!verificationToken.isUsable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Activation token expired or already used");
        }

        AppUser user = verificationToken.getUser();
        user.setEnabled(true);
        verificationToken.markAsUsed();
    }

    @Transactional
    public void requestPasswordReset(PasswordResetRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        AppUser user = appUserRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
        if (user == null) {
            return;
        }

        passwordResetTokenRepository.deleteByUser(user);
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusSeconds(passwordResetProperties.tokenExpirationHours() * 3600);
        passwordResetTokenRepository.save(new PasswordResetToken(token, user, expiresAt));

        passwordResetEmailService.sendPasswordResetEmail(normalizedEmail, token);
    }

    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid password reset token"));

        if (!resetToken.isUsable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password reset token expired or already used");
        }

        AppUser user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        resetToken.markAsUsed();
    }
}
