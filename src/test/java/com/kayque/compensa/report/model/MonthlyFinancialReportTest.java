package com.kayque.compensa.report.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MonthlyFinancialReportTest {

    @Test
    void shouldCreateMonthlyReport() {
        MonthlyFinancialReport report =
                new MonthlyFinancialReport(
                        YearMonth.of(2026, 7),
                        9,
                        6,
                        2,
                        1,
                        new BigDecimal("457.00"),
                        new BigDecimal("100.00"),
                        3342
                );

        assertEquals(9, report.totalDecisions());
        assertEquals(
                new BigDecimal("457.00"),
                report.purchasedValue()
        );
        assertTrue(report.hasDecisions());
    }

    @Test
    void shouldCreateEmptyReport() {
        MonthlyFinancialReport report =
                MonthlyFinancialReport.empty(
                        YearMonth.of(2026, 7)
                );

        assertFalse(report.hasDecisions());
        assertEquals(BigDecimal.ZERO, report.purchasedValue());
        assertEquals(BigDecimal.ZERO, report.preservedValue());
    }

    @Test
    void shouldRejectNegativeValues() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MonthlyFinancialReport(
                        YearMonth.of(2026, 7),
                        1,
                        1,
                        0,
                        0,
                        new BigDecimal("-1"),
                        BigDecimal.ZERO,
                        10
                )
        );
    }

    @Test
    void shouldRejectInconsistentDecisionTotals() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MonthlyFinancialReport(
                        YearMonth.of(2026, 7),
                        2,
                        2,
                        1,
                        0,
                        BigDecimal.TEN,
                        BigDecimal.ONE,
                        10
                )
        );
    }
}