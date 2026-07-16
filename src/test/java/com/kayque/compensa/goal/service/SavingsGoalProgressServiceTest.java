package com.kayque.compensa.goal.service;

import com.kayque.compensa.goal.model.SavingsGoal;
import com.kayque.compensa.goal.model.SavingsGoalProgress;
import com.kayque.compensa.goal.model.SavingsGoalProgressStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SavingsGoalProgressServiceTest {

    private final SavingsGoalProgressService service =
            new SavingsGoalProgressService();

    @Test
    void shouldCalculateGoalNotStarted() {
        SavingsGoal goal = new SavingsGoal(
                "Montar meu computador",
                new BigDecimal("5000"),
                BigDecimal.ZERO
        );

        SavingsGoalProgress progress =
                service.calculate(goal);

        assertEquals(
                new BigDecimal("0.00"),
                progress.percentage()
        );

        assertEquals(
                new BigDecimal("5000"),
                progress.remainingAmount()
        );

        assertEquals(
                SavingsGoalProgressStatus.NOT_STARTED,
                progress.status()
        );
    }

    @Test
    void shouldCalculateGoalInProgress() {
        SavingsGoal goal = new SavingsGoal(
                "Montar meu computador",
                new BigDecimal("5000"),
                new BigDecimal("1200")
        );

        SavingsGoalProgress progress =
                service.calculate(goal);

        assertEquals(
                new BigDecimal("24.00"),
                progress.percentage()
        );

        assertEquals(
                new BigDecimal("3800"),
                progress.remainingAmount()
        );

        assertEquals(
                SavingsGoalProgressStatus.IN_PROGRESS,
                progress.status()
        );
    }

    @Test
    void shouldCalculateCompletedGoal() {
        SavingsGoal goal = new SavingsGoal(
                "Viagem",
                new BigDecimal("3000"),
                new BigDecimal("3000")
        );

        SavingsGoalProgress progress =
                service.calculate(goal);

        assertEquals(
                new BigDecimal("100.00"),
                progress.percentage()
        );

        assertEquals(
                BigDecimal.ZERO,
                progress.remainingAmount()
        );

        assertEquals(
                SavingsGoalProgressStatus.COMPLETED,
                progress.status()
        );
    }

    @Test
    void shouldLimitPercentageWhenSavedAmountExceedsTarget() {
        SavingsGoal goal = new SavingsGoal(
                "Notebook",
                new BigDecimal("4000"),
                new BigDecimal("5000")
        );

        SavingsGoalProgress progress =
                service.calculate(goal);

        assertEquals(
                new BigDecimal("100"),
                progress.percentage()
        );

        assertEquals(
                BigDecimal.ZERO,
                progress.remainingAmount()
        );

        assertEquals(
                SavingsGoalProgressStatus.COMPLETED,
                progress.status()
        );
    }
}