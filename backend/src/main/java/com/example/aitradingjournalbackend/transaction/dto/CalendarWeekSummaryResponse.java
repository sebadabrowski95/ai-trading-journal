package com.example.aitradingjournalbackend.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CalendarWeekSummaryResponse(
    LocalDate weekStart,
    LocalDate weekEnd,
    BigDecimal grossPl,
    int tradeCount
) {
}
