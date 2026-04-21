package com.example.aitradingjournalbackend.dashboard;

import com.example.aitradingjournalbackend.auth.AppUserDetails;
import com.example.aitradingjournalbackend.dashboard.dto.DashboardRangeRequest;
import com.example.aitradingjournalbackend.dashboard.dto.DashboardResponse;
import com.example.aitradingjournalbackend.dashboard.dto.DashboardTransactionResponse;
import com.example.aitradingjournalbackend.transaction.Transaction;
import com.example.aitradingjournalbackend.transaction.repo.TransactionRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DashboardService {

    private final TransactionRepository transactionRepository;

    public DashboardService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getChartsData(DashboardRangeRequest request, Authentication authentication) {
        validateRange(request.dateFrom(), request.dateTo());

        Long userId = currentUserId(authentication);
        List<Transaction> transactions = transactionRepository
            .findAllByUserIdAndCloseTimeGreaterThanEqualAndCloseTimeLessThanOrderByCloseTimeAsc(
                userId,
                toUtcStart(request.dateFrom()),
                toUtcStart(request.dateTo().plusDays(1))
            );

        return new DashboardResponse(
            transactions.stream()
                .map(this::toDashboardTransaction)
                .toList(),
            transactions.stream()
                .map(Transaction::getSymbol)
                .filter(symbol -> symbol != null && !symbol.isBlank())
                .distinct()
                .sorted()
                .toList()
        );
    }

    private DashboardTransactionResponse toDashboardTransaction(Transaction transaction) {
        return new DashboardTransactionResponse(
            transaction.getCloseTime(),
            transaction.getSymbol(),
            transaction.getType(),
            transaction.getGrossPl()
        );
    }

    private void validateRange(LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom == null || dateTo == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dateFrom and dateTo are required");
        }
        if (dateFrom.isAfter(dateTo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dateFrom must be before or equal to dateTo");
        }
    }

    private Long currentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails userDetails)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return userDetails.getUserId();
    }

    private Instant toUtcStart(LocalDate date) {
        return date.atStartOfDay().toInstant(ZoneOffset.UTC);
    }
}
