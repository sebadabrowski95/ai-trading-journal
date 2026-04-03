package com.example.aitradingjournalbackend.transaction;

import com.example.aitradingjournalbackend.auth.AppUserDetails;
import com.example.aitradingjournalbackend.transaction.dto.CalendarDaySummaryResponse;
import com.example.aitradingjournalbackend.transaction.dto.CalendarMonthSummaryResponse;
import com.example.aitradingjournalbackend.transaction.dto.CalendarWeekSummaryResponse;
import com.example.aitradingjournalbackend.transaction.dto.CreateTransactionRequest;
import com.example.aitradingjournalbackend.transaction.dto.TransactionCalendarResponse;
import com.example.aitradingjournalbackend.transaction.dto.TransactionDayDetailsResponse;
import com.example.aitradingjournalbackend.transaction.dto.TransactionImportResponse;
import com.example.aitradingjournalbackend.transaction.dto.TransactionResponse;
import com.example.aitradingjournalbackend.transaction.repo.TransactionRepository;
import com.example.aitradingjournalbackend.user.AppUser;
import com.example.aitradingjournalbackend.user.repo.AppUserRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AppUserRepository appUserRepository;
    private final TransactionExcelImportService transactionExcelImportService;

    @Transactional(readOnly = true)
    public List<TransactionResponse> findCurrentUserTransactions(Authentication authentication) {
        Long userId = currentUserId(authentication);
        return transactionRepository.findAllByUserIdOrderByOpenTimeDesc(userId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse findCurrentUserTransaction(Long id, Authentication authentication) {
        Long userId = currentUserId(authentication);
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
        return toResponse(transaction);
    }

    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request, Authentication authentication) {
        AppUser user = currentUser(authentication);
        Transaction transaction = transactionRepository.save(new Transaction(
            user,
            request.position(),
            request.symbol(),
            request.type(),
            request.volume(),
            request.openTime(),
            request.openPrice(),
            request.closeTime(),
            request.closePrice(),
            request.sl(),
            request.tp(),
            request.margin(),
            request.commission(),
            request.swap(),
            request.rollover(),
            request.grossPl(),
            request.comment()
        ));
        return toResponse(transaction);
    }

    @Transactional
    public TransactionImportResponse importTransactions(org.springframework.web.multipart.MultipartFile file,
                                                        Authentication authentication) {
        AppUser user = currentUser(authentication);
        int importedCount = transactionExcelImportService.importExcel(file, user);
        return new TransactionImportResponse(importedCount);
    }

    @Transactional(readOnly = true)
    public TransactionCalendarResponse getCalendar(int year, int month, Authentication authentication) {
        YearMonth yearMonth = validateYearMonth(year, month);
        Long userId = currentUserId(authentication);

        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();
        LocalDate calendarStart = monthStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate calendarEnd = monthEnd.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<Transaction> transactions = transactionRepository
            .findAllByUserIdAndCloseTimeGreaterThanEqualAndCloseTimeLessThanOrderByCloseTimeAsc(
                userId,
                toUtcStart(calendarStart),
                toUtcStart(calendarEnd.plusDays(1))
            );

        Map<LocalDate, List<Transaction>> byDate = groupByCloseDate(transactions);
        List<CalendarDaySummaryResponse> days = new ArrayList<>();
        for (LocalDate date = monthStart; !date.isAfter(monthEnd); date = date.plusDays(1)) {
            List<Transaction> dayTransactions = byDate.getOrDefault(date, List.of());
            days.add(new CalendarDaySummaryResponse(date, sumGrossPl(dayTransactions), dayTransactions.size()));
        }

        List<CalendarWeekSummaryResponse> weeks = new ArrayList<>();
        for (LocalDate weekStart = calendarStart; !weekStart.isAfter(calendarEnd); weekStart = weekStart.plusWeeks(1)) {
            LocalDate weekEnd = weekStart.plusDays(6);
            List<Transaction> weekTransactions = new ArrayList<>();
            for (LocalDate date = weekStart; !date.isAfter(weekEnd); date = date.plusDays(1)) {
                weekTransactions.addAll(byDate.getOrDefault(date, List.of()));
            }
            weeks.add(new CalendarWeekSummaryResponse(
                weekStart,
                weekEnd,
                sumGrossPl(weekTransactions),
                weekTransactions.size()
            ));
        }

        List<Transaction> monthTransactions = new ArrayList<>();
        for (LocalDate date = monthStart; !date.isAfter(monthEnd); date = date.plusDays(1)) {
            monthTransactions.addAll(byDate.getOrDefault(date, List.of()));
        }

        return TransactionCalendarResponse.of(
            yearMonth,
            days,
            weeks,
            new CalendarMonthSummaryResponse(sumGrossPl(monthTransactions), monthTransactions.size())
        );
    }

    @Transactional(readOnly = true)
    public TransactionDayDetailsResponse getDayDetails(LocalDate date, Authentication authentication) {
        Long userId = currentUserId(authentication);
        List<Transaction> transactions = transactionRepository
            .findAllByUserIdAndCloseTimeGreaterThanEqualAndCloseTimeLessThanOrderByCloseTimeDesc(
                userId,
                toUtcStart(date),
                toUtcStart(date.plusDays(1))
            );

        return new TransactionDayDetailsResponse(
            date,
            sumGrossPl(transactions),
            transactions.size(),
            transactions.stream()
                .map(this::toResponse)
                .toList()
        );
    }

    @Transactional
    public void deleteCurrentUserTransaction(Long id, Authentication authentication) {
        Long userId = currentUserId(authentication);
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
        transactionRepository.delete(transaction);
    }

    private AppUser currentUser(Authentication authentication) {
        Long userId = currentUserId(authentication);
        return appUserRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
    }

    private Long currentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails userDetails)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return userDetails.getUserId();
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
            transaction.getId(),
            transaction.getPosition(),
            transaction.getSymbol(),
            transaction.getType(),
            transaction.getVolume(),
            transaction.getOpenTime(),
            transaction.getOpenPrice(),
            transaction.getCloseTime(),
            transaction.getClosePrice(),
            transaction.getSl(),
            transaction.getTp(),
            transaction.getMargin(),
            transaction.getCommission(),
            transaction.getSwap(),
            transaction.getRollover(),
            transaction.getGrossPl(),
            transaction.getComment()
        );
    }

    private YearMonth validateYearMonth(int year, int month) {
        try {
            return YearMonth.of(year, month);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid year or month");
        }
    }

    private Instant toUtcStart(LocalDate date) {
        return date.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    private Map<LocalDate, List<Transaction>> groupByCloseDate(List<Transaction> transactions) {
        Map<LocalDate, List<Transaction>> byDate = new LinkedHashMap<>();
        transactions.stream()
            .filter(transaction -> transaction.getCloseTime() != null)
            .sorted(Comparator.comparing(Transaction::getCloseTime))
            .forEach(transaction -> {
                LocalDate closeDate = transaction.getCloseTime().atOffset(ZoneOffset.UTC).toLocalDate();
                byDate.computeIfAbsent(closeDate, ignored -> new ArrayList<>()).add(transaction);
            });
        return byDate;
    }

    private BigDecimal sumGrossPl(List<Transaction> transactions) {
        return transactions.stream()
            .map(Transaction::getGrossPl)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
