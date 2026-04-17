package com.example.aitradingjournalbackend.AI;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionSummary(
        String position,
        String symbol,
        String type,
        BigDecimal volume,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
        Instant openTime,
        BigDecimal openPrice,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
        Instant closeTime,
        BigDecimal closePrice,
        BigDecimal grossPl,
        BigDecimal sl,
        BigDecimal tp,
        BigDecimal commission,
        BigDecimal swap,
        BigDecimal rollover,
        String comment
) {}