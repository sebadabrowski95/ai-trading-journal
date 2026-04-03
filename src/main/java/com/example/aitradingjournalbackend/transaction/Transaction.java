package com.example.aitradingjournalbackend.transaction;

import com.example.aitradingjournalbackend.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "transactions",
    indexes = {
        @Index(name = "idx_transactions_user_id", columnList = "user_id"),
        @Index(name = "idx_transactions_user_open_time", columnList = "user_id, openTime")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false)
    private String position;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal volume;

    @Column(nullable = false)
    private Instant openTime;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal openPrice;

    @Column
    private Instant closeTime;

    @Column(precision = 19, scale = 8)
    private BigDecimal closePrice;

    @Column(precision = 19, scale = 8)
    private BigDecimal sl;

    @Column(precision = 19, scale = 8)
    private BigDecimal tp;

    @Column(precision = 19, scale = 2)
    private BigDecimal margin;

    @Column(precision = 19, scale = 2)
    private BigDecimal commission;

    @Column(precision = 19, scale = 2)
    private BigDecimal swap;

    @Column(precision = 19, scale = 2)
    private BigDecimal rollover;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal grossPl;

    @Column(length = 2000)
    private String comment;

    public Transaction(AppUser user,
                       String position,
                       String symbol,
                       String type,
                       BigDecimal volume,
                       Instant openTime,
                       BigDecimal openPrice,
                       Instant closeTime,
                       BigDecimal closePrice,
                       BigDecimal sl,
                       BigDecimal tp,
                       BigDecimal margin,
                       BigDecimal commission,
                       BigDecimal swap,
                       BigDecimal rollover,
                       BigDecimal grossPl,
                       String comment) {
        this.user = user;
        this.position = position;
        this.symbol = symbol;
        this.type = type;
        this.volume = volume;
        this.openTime = openTime;
        this.openPrice = openPrice;
        this.closeTime = closeTime;
        this.closePrice = closePrice;
        this.sl = sl;
        this.tp = tp;
        this.margin = margin;
        this.commission = commission;
        this.swap = swap;
        this.rollover = rollover;
        this.grossPl = grossPl;
        this.comment = comment;
    }
}
