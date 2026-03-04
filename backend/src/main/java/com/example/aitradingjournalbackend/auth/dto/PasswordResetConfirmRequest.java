package com.example.aitradingjournalbackend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordResetConfirmRequest(
    @NotBlank String token,
    @NotBlank
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,128}$",
        message = "Password must be at least 8 characters and include a lowercase letter, uppercase letter, digit, and special character"
    )
    String newPassword
) {
    @Override
    public String toString() {
        return "PasswordResetConfirmRequest[token=<redacted>, newPassword=<redacted>]";
    }
}
