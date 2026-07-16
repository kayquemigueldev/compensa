package com.kayque.compensa.goal.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SavingsGoalTest {

    @Test
    void shouldCreateSavingsGoal() {
        SavingsGoal goal = new SavingsGoal(
                "  Montar meu computador  ",
                new BigDecimal("5000"),
                new BigDecimal("1200")
        );

        assertEquals(
                "Montar meu computador",
                goal.name()
        );

        assertEquals(
                new BigDecimal("3800"),
                goal.remainingAmount()
        );

        assertFalse(goal.isCompleted());
    }

    @Test
    void shouldUseZeroWhenSavedAmountIsNull() {
        SavingsGoal goal = new SavingsGoal(
                "Viajar",
                new BigDecimal("3000"),
                null
        );

        assertEquals(
                BigDecimal.ZERO,
                goal.savedAmount()
        );
    }

    @Test
    void shouldRecognizeCompletedGoal() {
        SavingsGoal goal = new SavingsGoal(
                "Notebook",
                new BigDecimal("4000"),
                new BigDecimal("4500")
        );

        assertTrue(goal.isCompleted());

        assertEquals(
                BigDecimal.ZERO,
                goal.remainingAmount()
        );
    }

    @Test
    void shouldRejectGoalWithoutName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SavingsGoal(
                        " ",
                        new BigDecimal("5000"),
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void shouldRejectInvalidAmounts() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SavingsGoal(
                        "Computador",
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new SavingsGoal(
                        "Computador",
                        new BigDecimal("5000"),
                        new BigDecimal("-100")
                )
        );
    }
}