package com.example.aitradingjournalbackend.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CalendarDaySummaryResponse(
    LocalDate date,
    BigDecimal grossPl,
    int tradeCount
) {
}
