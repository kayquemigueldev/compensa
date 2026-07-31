package com.kayque.compensa.goal.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SavingsGoalAchievementTest {

    private static final LocalDateTime CREATED_AT =
            LocalDateTime.of(
                    2026,
                    1,
                    10,
                    10,
                    0
            );

    private static final LocalDateTime COMPLETED_AT =
            LocalDateTime.of(
                    2026,
                    7,
                    30,
                    20,
                    0
            );

    @Test
    void shouldCreateCompletedAchievement() {
        SavingsGoalAchievement achievement =
                createAchievement(
                        new BigDecimal("8000"),
                        new BigDecimal("8000")
                );

        assertEquals(
                "Montar meu computador",
                achievement.name()
        );

        assertFalse(achievement.exceededTarget());
        assertEquals(
                BigDecimal.ZERO,
                achievement.exceededAmount()
        );
    }

    @Test
    void shouldCalculateAmountAboveTarget() {
        SavingsGoalAchievement achievement =
                createAchievement(
                        new BigDecimal("8000"),
                        new BigDecimal("8250")
                );

        assertTrue(achievement.exceededTarget());

        assertEquals(
                new BigDecimal("250"),
                achievement.exceededAmount()
        );
    }

    @Test
    void shouldIdentifyTargetDate() {
        SavingsGoalAchievement achievement =
                createAchievement(
                        new BigDecimal("8000"),
                        new BigDecimal("8000")
                );

        assertTrue(achievement.hadTargetDate());
    }

    @Test
    void shouldRejectAchievementBelowTarget() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createAchievement(
                        new BigDecimal("8000"),
                        new BigDecimal("7999")
                )
        );
    }

    @Test
    void shouldRejectCompletionBeforeCreation() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SavingsGoalAchievement(
                        1,
                        "Montar meu computador",
                        new BigDecimal("8000"),
                        new BigDecimal("8000"),
                        LocalDate.of(2026, 12, 31),
                        COMPLETED_AT,
                        CREATED_AT
                )
        );
    }

    private SavingsGoalAchievement createAchievement(
            BigDecimal targetAmount,
            BigDecimal savedAmount
    ) {
        return new SavingsGoalAchievement(
                1,
                "Montar meu computador",
                targetAmount,
                savedAmount,
                LocalDate.of(2026, 12, 31),
                CREATED_AT,
                COMPLETED_AT
        );
    }
}