package com.example.aitradingjournalbackend.auth;

import com.example.aitradingjournalbackend.auth.dto.RegisterRequest;
import com.example.aitradingjournalbackend.user.AppUser;
import com.example.aitradingjournalbackend.user.EmailVerificationToken;
import com.example.aitradingjournalbackend.user.repo.AppUserRepository;
import com.example.aitradingjournalbackend.user.repo.EmailVerificationTokenRepository;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivationEmailService activationEmailService;
    private final ActivationProperties activationProperties;

    @Transactional
    public void register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        if (appUserRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
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
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid activation token"));

        if (!verificationToken.isUsable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Activation token expired or already used");
        }

        AppUser user = verificationToken.getUser();
        user.setEnabled(true);
        verificationToken.markAsUsed();
    }
}
