package com.kayque.compensa.goal.service;

import com.kayque.compensa.goal.model.SavingsGoal;
import com.kayque.compensa.goal.model.SavingsGoalProgress;
import com.kayque.compensa.goal.model.SavingsGoalProgressStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class SavingsGoalProgressService {

    private static final BigDecimal ONE_HUNDRED =
            new BigDecimal("100");

    public SavingsGoalProgress calculate(
            SavingsGoal goal
    ) {
        Objects.requireNonNull(
                goal,
                "O objetivo é obrigatório."
        );

        BigDecimal percentage = goal.savedAmount()
                .multiply(ONE_HUNDRED)
                .divide(
                        goal.targetAmount(),
                        2,
                        RoundingMode.HALF_UP
                )
                .min(ONE_HUNDRED);

        SavingsGoalProgressStatus status =
                determineStatus(goal);

        return new SavingsGoalProgress(
                goal.targetAmount(),
                goal.savedAmount(),
                goal.remainingAmount(),
                percentage,
                status
        );
    }

    private SavingsGoalProgressStatus determineStatus(
            SavingsGoal goal
    ) {
        if (goal.isCompleted()) {
            return SavingsGoalProgressStatus.COMPLETED;
        }

        if (goal.savedAmount().compareTo(
                BigDecimal.ZERO
        ) == 0) {
            return SavingsGoalProgressStatus.NOT_STARTED;
        }

        return SavingsGoalProgressStatus.IN_PROGRESS;
    }
}