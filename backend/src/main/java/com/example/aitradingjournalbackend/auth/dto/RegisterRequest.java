package com.example.aitradingjournalbackend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
    @NotBlank @Email String email,
    @NotBlank
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,128}$",
        message = "Hasło musi mieć minimum 8 znaków oraz zawierać małą literę, dużą literę, cyfrę i znak specjalny"
    )
    String password
) {
}
