package com.example.aitradingjournalbackend.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.password-reset")
public record PasswordResetProperties(
    String baseUrl,
    String fromEmail,
    long tokenExpirationHours
) {
}
