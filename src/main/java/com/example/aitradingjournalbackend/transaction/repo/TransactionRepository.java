package com.example.aitradingjournalbackend.transaction.repo;

import com.example.aitradingjournalbackend.transaction.Transaction;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByUserIdAndPosition(Long userId, String position);
    List<Transaction> findAllByUserIdOrderByOpenTimeDesc(Long userId);
    List<Transaction> findTop70ByUserIdAndCloseTimeIsNotNullOrderByCloseTimeDesc(Long userId);

    List<Transaction> findAllByUserIdAndCloseTimeGreaterThanEqualAndCloseTimeLessThanOrderByCloseTimeAsc(
        Long userId,
        Instant closeTimeFrom,
        Instant closeTimeTo
    );

    List<Transaction> findAllByUserIdAndCloseTimeGreaterThanEqualAndCloseTimeLessThanOrderByCloseTimeDesc(
        Long userId,
        Instant closeTimeFrom,
        Instant closeTimeTo
    );

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);
}
