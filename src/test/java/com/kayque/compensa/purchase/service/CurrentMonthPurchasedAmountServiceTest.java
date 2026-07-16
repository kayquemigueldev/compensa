package com.kayque.compensa.purchase.service;

import com.kayque.compensa.purchase.repository.PurchasedAmountRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CurrentMonthPurchasedAmountServiceTest {

    @Test
    void shouldQueryCurrentMonthUsingLocalTimezone() {
        AtomicReference<Instant> capturedStart =
                new AtomicReference<>();

        AtomicReference<Instant> capturedEnd =
                new AtomicReference<>();

        PurchasedAmountRepository repository =
                (startInclusive, endExclusive) -> {
                    capturedStart.set(startInclusive);
                    capturedEnd.set(endExclusive);

                    return new BigDecimal("7.99");
                };

        Clock clock = Clock.fixed(
                Instant.parse("2026-07-15T15:00:00Z"),
                ZoneId.of("America/Sao_Paulo")
        );

        CurrentMonthPurchasedAmountService service =
                new CurrentMonthPurchasedAmountService(
                        repository,
                        clock
                );

        BigDecimal purchasedAmount =
                service.calculate();

        assertEquals(
                new BigDecimal("7.99"),
                purchasedAmount
        );

        assertEquals(
                Instant.parse("2026-07-01T03:00:00Z"),
                capturedStart.get()
        );

        assertEquals(
                Instant.parse("2026-08-01T03:00:00Z"),
                capturedEnd.get()
        );
    }
}