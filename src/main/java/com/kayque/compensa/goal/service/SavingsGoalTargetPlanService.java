package com.kayque.compensa.goal.service;

import com.kayque.compensa.goal.model.SavingsGoal;
import com.kayque.compensa.goal.model.SavingsGoalTargetPlan;
import com.kayque.compensa.goal.model.SavingsGoalTargetPlanStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class SavingsGoalTargetPlanService {

    private static final int MONEY_SCALE = 2;

    public SavingsGoalTargetPlan calculate(
            SavingsGoal goal,
            LocalDate currentDate
    ) {
        Objects.requireNonNull(
                goal,
                "O objetivo é obrigatório."
        );

        Objects.requireNonNull(
                currentDate,
                "A data atual é obrigatória."
        );

        BigDecimal remainingAmount =
                goal.remainingAmount();

        if (goal.isCompleted()) {
            return createPlan(
                    SavingsGoalTargetPlanStatus.COMPLETED,
                    BigDecimal.ZERO,
                    0,
                    BigDecimal.ZERO
            );
        }

        if (!goal.hasTargetDate()) {
            return createPlan(
                    SavingsGoalTargetPlanStatus.NO_TARGET_DATE,
                    remainingAmount,
                    0,
                    BigDecimal.ZERO
            );
        }

        LocalDate targetDate = goal.targetDate();

        if (targetDate.isBefore(currentDate)) {
            return createPlan(
                    SavingsGoalTargetPlanStatus.DEADLINE_PASSED,
                    remainingAmount,
                    0,
                    BigDecimal.ZERO
            );
        }

        long monthsAvailable = calculateMonthsAvailable(
                currentDate,
                targetDate
        );

        BigDecimal requiredMonthlyAmount =
                remainingAmount.divide(
                        BigDecimal.valueOf(monthsAvailable),
                        MONEY_SCALE,
                        RoundingMode.CEILING
                );

        return createPlan(
                SavingsGoalTargetPlanStatus.ACTIVE,
                remainingAmount,
                monthsAvailable,
                requiredMonthlyAmount
        );
    }

    private long calculateMonthsAvailable(
            LocalDate currentDate,
            LocalDate targetDate
    ) {
        YearMonth currentMonth =
                YearMonth.from(currentDate);

        YearMonth targetMonth =
                YearMonth.from(targetDate);

        return ChronoUnit.MONTHS.between(
                currentMonth,
                targetMonth
        ) + 1;
    }

    private SavingsGoalTargetPlan createPlan(
            SavingsGoalTargetPlanStatus status,
            BigDecimal remainingAmount,
            long monthsAvailable,
            BigDecimal requiredMonthlyAmount
    ) {
        return new SavingsGoalTargetPlan(
                status,
                remainingAmount,
                monthsAvailable,
                requiredMonthlyAmount
        );
    }
}