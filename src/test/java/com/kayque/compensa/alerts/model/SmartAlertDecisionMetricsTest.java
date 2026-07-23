package com.kayque.compensa.alerts.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SmartAlertDecisionMetricsTest {

    @Test
    void shouldCreateValidMetrics() {
        SmartAlertDecisionMetrics metrics =
                new SmartAlertDecisionMetrics(
                        4,
                        2,
                        3,
                        1,
                        720,
                        new BigDecimal("2350.00")
                );

        assertEquals(4, metrics.purchasesMadeThisMonth());
        assertEquals(2, metrics.purchasesAvoidedThisMonth());
        assertEquals(3, metrics.pendingDecisions());
        assertEquals(1, metrics.overduePendingDecisions());
        assertEquals(720, metrics.workMinutesThisMonth());

        assertEquals(
                new BigDecimal("2350.00"),
                metrics.preservedAmountThisYear()
        );
    }

    @Test
    void shouldRejectNegativePurchasesMade() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createMetrics(-1, 0, 0, 0, 0)
        );
    }

    @Test
    void shouldRejectNegativePurchasesAvoided() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createMetrics(0, -1, 0, 0, 0)
        );
    }

    @Test
    void shouldRejectNegativePendingDecisions() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createMetrics(0, 0, -1, 0, 0)
        );
    }

    @Test
    void shouldRejectMoreOverdueThanPendingDecisions() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createMetrics(0, 0, 1, 2, 0)
        );
    }

    @Test
    void shouldRejectNegativeWorkMinutes() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createMetrics(0, 0, 0, 0, -1)
        );
    }

    @Test
    void shouldRejectNegativePreservedAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SmartAlertDecisionMetrics(
                        0,
                        0,
                        0,
                        0,
                        0,
                        new BigDecimal("-0.01")
                )
        );
    }

    private SmartAlertDecisionMetrics createMetrics(
            int purchasesMade,
            int purchasesAvoided,
            int pending,
            int overdue,
            long workMinutes
    ) {
        return new SmartAlertDecisionMetrics(
                purchasesMade,
                purchasesAvoided,
                pending,
                overdue,
                workMinutes,
                BigDecimal.ZERO
        );
    }
}