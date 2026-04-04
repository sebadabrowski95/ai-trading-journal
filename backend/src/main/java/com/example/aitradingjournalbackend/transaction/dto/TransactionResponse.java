package com.example.aitradingjournalbackend.transaction.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
    Long id,
    String position,
    String symbol,
    String type,
    BigDecimal volume,
    Instant openTime,
    BigDecimal openPrice,
    Instant closeTime,
    BigDecimal closePrice,
    BigDecimal sl,
    BigDecimal tp,
    BigDecimal margin,
    BigDecimal commission,
    BigDecimal swap,
    BigDecimal rollover,
    BigDecimal grossPl,
    String comment
) {
}
