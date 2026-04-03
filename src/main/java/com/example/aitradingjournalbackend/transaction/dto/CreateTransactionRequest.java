package com.example.aitradingjournalbackend.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public record CreateTransactionRequest(
    @NotBlank String position,
    @NotBlank String symbol,
    @NotBlank String type,
    @NotNull BigDecimal volume,
    @NotNull Instant openTime,
    @NotNull BigDecimal openPrice,
    Instant closeTime,
    BigDecimal closePrice,
    BigDecimal sl,
    BigDecimal tp,
    BigDecimal margin,
    BigDecimal commission,
    BigDecimal swap,
    BigDecimal rollover,
    @NotNull BigDecimal grossPl,
    String comment
) {
}
