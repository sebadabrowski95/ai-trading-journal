package com.example.aitradingjournalbackend.transaction.dto;

import java.time.YearMonth;
import java.util.List;

public record TransactionCalendarResponse(
    int year,
    int month,
    List<CalendarDaySummaryResponse> days,
    List<CalendarWeekSummaryResponse> weeks,
    CalendarMonthSummaryResponse monthSummary
) {
    public static TransactionCalendarResponse of(YearMonth yearMonth,
                                                 List<CalendarDaySummaryResponse> days,
                                                 List<CalendarWeekSummaryResponse> weeks,
                                                 CalendarMonthSummaryResponse monthSummary) {
        return new TransactionCalendarResponse(yearMonth.getYear(), yearMonth.getMonthValue(), days, weeks, monthSummary);
    }
}
