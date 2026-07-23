package com.kayque.compensa.alerts.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmartAlertSnapshotTest {

    @Test
    void shouldCreateCompleteSnapshot() {
        SmartAlertSnapshot snapshot =
                createSnapshot(
                        new BigDecimal("82"),
                        new BigDecimal("120"),
                        new BigDecimal("400"),
                        new BigDecimal("500"),
                        new BigDecimal("35"),
                        new BigDecimal("30"),
                        5,
                        3,
                        2,
                        1,
                        600,
                        new BigDecimal("2350")
                );

        assertTrue(snapshot.hasMonthlyGoal());
        assertTrue(snapshot.hasFinancialGoal());
        assertTrue(snapshot.hasPendingDecisions());
        assertTrue(snapshot.hasOverduePendingDecisions());
    }

    @Test
    void shouldAllowNegativeAvailableBudget() {
        SmartAlertSnapshot snapshot =
                createSnapshot(
                        new BigDecimal("120"),
                        new BigDecimal("-200"),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        1,
                        0,
                        0,
                        0,
                        120,
                        BigDecimal.ZERO
                );

        assertFalse(snapshot.hasMonthlyGoal());
        assertFalse(snapshot.hasFinancialGoal());
    }

    @Test
    void shouldIdentifyMissingMonthlyGoal() {
        SmartAlertSnapshot snapshot =
                createSnapshot(
                        new BigDecimal("20"),
                        new BigDecimal("800"),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        0,
                        0,
                        0,
                        0,
                        0,
                        BigDecimal.ZERO
                );

        assertFalse(snapshot.hasMonthlyGoal());
    }

    @Test
    void shouldRejectNegativeBudgetPercentage() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createSnapshot(
                        new BigDecimal("-1"),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        0,
                        0,
                        0,
                        0,
                        0,
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void shouldRejectNegativePurchaseCount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createSnapshot(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        -1,
                        0,
                        0,
                        0,
                        0,
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void shouldRejectNegativeWorkTime() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createSnapshot(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        0,
                        0,
                        0,
                        0,
                        -1,
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void shouldRejectOverdueCountGreaterThanPendingCount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createSnapshot(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        0,
                        0,
                        1,
                        2,
                        0,
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void shouldRejectMissingMonetaryValue() {
        assertThrows(
                NullPointerException.class,
                () -> createSnapshot(
                        BigDecimal.ZERO,
                        null,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        0,
                        0,
                        0,
                        0,
                        0,
                        BigDecimal.ZERO
                )
        );
    }

    private SmartAlertSnapshot createSnapshot(
            BigDecimal budgetUsagePercentage,
            BigDecimal availableBudget,
            BigDecimal monthlyGoalContributions,
            BigDecimal monthlyGoalTarget,
            BigDecimal goalProgressPercentage,
            BigDecimal expectedGoalProgressPercentage,
            int purchasesMade,
            int purchasesAvoided,
            int pendingDecisions,
            int overduePendingDecisions,
            long totalWorkMinutes,
            BigDecimal preservedAmountThisYear
    ) {
        return new SmartAlertSnapshot(
                budgetUsagePercentage,
                availableBudget,
                monthlyGoalContributions,
                monthlyGoalTarget,
                goalProgressPercentage,
                expectedGoalProgressPercentage,
                purchasesMade,
                purchasesAvoided,
                pendingDecisions,
                overduePendingDecisions,
                totalWorkMinutes,
                preservedAmountThisYear
        );
    }
}