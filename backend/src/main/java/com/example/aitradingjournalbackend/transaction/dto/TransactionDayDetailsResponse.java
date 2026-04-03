package com.example.aitradingjournalbackend.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TransactionDayDetailsResponse(
    LocalDate date,
    BigDecimal grossPl,
    int tradeCount,
    List<TransactionResponse> transactions
) {
}
