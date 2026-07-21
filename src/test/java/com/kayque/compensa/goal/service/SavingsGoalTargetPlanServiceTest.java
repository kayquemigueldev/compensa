package com.kayque.compensa.goal.service;

import com.kayque.compensa.goal.model.SavingsGoal;
import com.kayque.compensa.goal.model.SavingsGoalTargetPlan;
import com.kayque.compensa.goal.model.SavingsGoalTargetPlanStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SavingsGoalTargetPlanServiceTest {

    private final SavingsGoalTargetPlanService service =
            new SavingsGoalTargetPlanService();

    @Test
    void shouldCalculateRequiredMonthlyAmount() {
        SavingsGoal goal = new SavingsGoal(
                "Montar meu computador",
                new BigDecimal("5000"),
                new BigDecimal("1250"),
                LocalDate.of(2026, 12, 20)
        );

        SavingsGoalTargetPlan plan = service.calculate(
                goal,
                LocalDate.of(2026, 7, 21)
        );

        assertEquals(
                SavingsGoalTargetPlanStatus.ACTIVE,
                plan.status()
        );

        assertEquals(
                new BigDecimal("3750"),
                plan.remainingAmount()
        );

        assertEquals(6, plan.monthsAvailable());

        assertEquals(
                new BigDecimal("625.00"),
                plan.requiredMonthlyAmount()
        );

        assertTrue(plan.hasMonthlyPlan());
    }

    @Test
    void shouldRoundMonthlyAmountUpToAvoidMissingCents() {
        SavingsGoal goal = new SavingsGoal(
                "Reserva",
                new BigDecimal("1000"),
                BigDecimal.ZERO,
                LocalDate.of(2026, 9, 30)
        );

        SavingsGoalTargetPlan plan = service.calculate(
                goal,
                LocalDate.of(2026, 7, 21)
        );

        assertEquals(3, plan.monthsAvailable());

        assertEquals(
                new BigDecimal("333.34"),
                plan.requiredMonthlyAmount()
        );
    }

    @Test
    void shouldReportMissingTargetDate() {
        SavingsGoal goal = new SavingsGoal(
                "Montar meu computador",
                new BigDecimal("5000"),
                new BigDecimal("1250"),
                null
        );

        SavingsGoalTargetPlan plan = service.calculate(
                goal,
                LocalDate.of(2026, 7, 21)
        );

        assertEquals(
                SavingsGoalTargetPlanStatus.NO_TARGET_DATE,
                plan.status()
        );

        assertEquals(0, plan.monthsAvailable());

        assertEquals(
                BigDecimal.ZERO,
                plan.requiredMonthlyAmount()
        );

        assertFalse(plan.hasMonthlyPlan());
    }

    @Test
    void shouldReportExpiredDeadline() {
        SavingsGoal goal = new SavingsGoal(
                "Montar meu computador",
                new BigDecimal("5000"),
                new BigDecimal("1250"),
                LocalDate.of(2026, 7, 20)
        );

        SavingsGoalTargetPlan plan = service.calculate(
                goal,
                LocalDate.of(2026, 7, 21)
        );

        assertEquals(
                SavingsGoalTargetPlanStatus.DEADLINE_PASSED,
                plan.status()
        );

        assertEquals(0, plan.monthsAvailable());

        assertFalse(plan.hasMonthlyPlan());
    }

    @Test
    void shouldReportCompletedGoal() {
        SavingsGoal goal = new SavingsGoal(
                "Montar meu computador",
                new BigDecimal("5000"),
                new BigDecimal("5000"),
                LocalDate.of(2026, 12, 20)
        );

        SavingsGoalTargetPlan plan = service.calculate(
                goal,
                LocalDate.of(2026, 7, 21)
        );

        assertEquals(
                SavingsGoalTargetPlanStatus.COMPLETED,
                plan.status()
        );

        assertEquals(
                BigDecimal.ZERO,
                plan.remainingAmount()
        );

        assertEquals(
                BigDecimal.ZERO,
                plan.requiredMonthlyAmount()
        );

        assertFalse(plan.hasMonthlyPlan());
    }
}