package com.example.aitradingjournalbackend.auth.dto;

public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresIn
) {
}
