package com.example.aitradingjournalbackend.auth.dto;

public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresIn
) {
    @Override
    public String toString() {
        return "AuthResponse[accessToken=<redacted>, tokenType=" + tokenType + ", expiresIn=" + expiresIn + "]";
    }
}
