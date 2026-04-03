package com.example.aitradingjournalbackend.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.activation")
public record ActivationProperties(
    String baseUrl,
    String fromEmail,
    long tokenExpirationHours
) {
}
