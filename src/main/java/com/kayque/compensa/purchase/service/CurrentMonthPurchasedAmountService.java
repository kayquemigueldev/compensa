package com.kayque.compensa.purchase.service;

import com.kayque.compensa.purchase.repository.PurchasedAmountRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Objects;

public class CurrentMonthPurchasedAmountService {

    private final PurchasedAmountRepository repository;
    private final Clock clock;

    public CurrentMonthPurchasedAmountService(
            PurchasedAmountRepository repository
    ) {
        this(
                repository,
                Clock.systemDefaultZone()
        );
    }

    public CurrentMonthPurchasedAmountService(
            PurchasedAmountRepository repository,
            Clock clock
    ) {
        this.repository = Objects.requireNonNull(
                repository,
                "O repository de compras é obrigatório."
        );

        this.clock = Objects.requireNonNull(
                clock,
                "O relógio é obrigatório."
        );
    }

    public BigDecimal calculate() {
        ZoneId zone = clock.getZone();
        YearMonth currentMonth = YearMonth.now(clock);

        Instant startInclusive =
                currentMonth
                        .atDay(1)
                        .atStartOfDay(zone)
                        .toInstant();

        Instant endExclusive =
                currentMonth
                        .plusMonths(1)
                        .atDay(1)
                        .atStartOfDay(zone)
                        .toInstant();

        return repository.sumPurchasedAmountBetween(
                startInclusive,
                endExclusive
        );
    }
}