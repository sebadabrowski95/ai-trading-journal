package com.example.aitradingjournalbackend.dashboard.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record DashboardRangeRequest(
    @NotNull LocalDate dateFrom,
    @NotNull LocalDate dateTo
) {
}
