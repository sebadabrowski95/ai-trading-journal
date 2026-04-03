package com.example.aitradingjournalbackend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ActivationConfirmRequest(
    @NotBlank String token
) {
    @Override
    public String toString() {
        return "ActivationConfirmRequest[token=<redacted>]";
    }
}
