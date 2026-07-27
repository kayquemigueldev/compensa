package com.kayque.compensa.dashboard.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardWeeklySummaryTest {

    @Test
    void shouldIdentifySummaryWithDecisions() {
        DashboardWeeklySummary summary =
                new DashboardWeeklySummary(
                        4,
                        3,
                        1,
                        new BigDecimal("180.00"),
                        new BigDecimal("50.00"),
                        240
                );

        assertTrue(summary.hasDecisions());
    }

    @Test
    void shouldIdentifyEmptySummary() {
        DashboardWeeklySummary summary =
                new DashboardWeeklySummary(
                        0,
                        0,
                        0,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        0
                );

        assertFalse(summary.hasDecisions());
    }

    @Test
    void shouldRejectNegativePurchasedValue() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DashboardWeeklySummary(
                        1,
                        1,
                        0,
                        new BigDecimal("-10.00"),
                        BigDecimal.ZERO,
                        30
                )
        );
    }

    @Test
    void shouldRejectInconsistentDecisionTotals() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DashboardWeeklySummary(
                        1,
                        1,
                        1,
                        new BigDecimal("20.00"),
                        new BigDecimal("10.00"),
                        60
                )
        );
    }
}