package com.example.aitradingjournalbackend.dashboard.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record DashboardTransactionResponse(
    Instant closeTime,
    String symbol,
    String type,
    BigDecimal grossPl
) {
}
