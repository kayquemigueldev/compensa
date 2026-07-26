package com.kayque.compensa.goal.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SavingsGoalContributionTest {

    @Test
    void shouldCreateValidContribution() {
        LocalDateTime contributedAt =
                LocalDateTime.of(
                        2026,
                        7,
                        17,
                        10,
                        30
                );

        SavingsGoalContribution contribution =
                new SavingsGoalContribution(
                        1,
                        new BigDecimal("250.00"),
                        contributedAt
                );

        assertEquals(
                new BigDecimal("250.00"),
                contribution.amount()
        );

        assertEquals(
                contributedAt,
                contribution.contributedAt()
        );
    }

    @Test
    void shouldRejectNonPositiveId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SavingsGoalContribution(
                        0,
                        new BigDecimal("100"),
                        LocalDateTime.now()
                )
        );
    }

    @Test
    void shouldRejectNonPositiveAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SavingsGoalContribution(
                        1,
                        BigDecimal.ZERO,
                        LocalDateTime.now()
                )
        );
    }
}