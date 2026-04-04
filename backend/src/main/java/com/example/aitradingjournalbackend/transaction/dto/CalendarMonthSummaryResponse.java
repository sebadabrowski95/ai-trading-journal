package com.example.aitradingjournalbackend.transaction.dto;

import java.math.BigDecimal;

public record CalendarMonthSummaryResponse(
    BigDecimal grossPl,
    int tradeCount
) {
}
