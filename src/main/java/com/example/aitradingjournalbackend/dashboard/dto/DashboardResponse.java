package com.example.aitradingjournalbackend.dashboard.dto;

import java.util.List;

public record DashboardResponse(
    List<DashboardTransactionResponse> transactions,
    List<String> symbols
) {
}
