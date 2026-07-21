package com.kayque.compensa.goal.model;

import java.math.BigDecimal;
import java.util.Objects;

public record SavingsGoalMonthlyPace(
        SavingsGoalMonthlyPaceStatus status,
        BigDecimal requiredMonthlyAmount,
        BigDecimal contributedThisMonth,
        BigDecimal remainingThisMonth
) {

    public SavingsGoalMonthlyPace {
        Objects.requireNonNull(
                status,
                "O status do ritmo mensal é obrigatório."
        );

        Objects.requireNonNull(
                requiredMonthlyAmount,
                "O valor mensal necessário é obrigatório."
        );

        Objects.requireNonNull(
                contributedThisMonth,
                "O valor contribuído no mês é obrigatório."
        );

        Objects.requireNonNull(
                remainingThisMonth,
                "O valor restante no mês é obrigatório."
        );

        validateNonNegative(
                requiredMonthlyAmount,
                "O valor mensal necessário não pode ser negativo."
        );

        validateNonNegative(
                contributedThisMonth,
                "O valor contribuído no mês não pode ser negativo."
        );

        validateNonNegative(
                remainingThisMonth,
                "O valor restante no mês não pode ser negativo."
        );
    }

    public boolean hasActivePlan() {
        return status !=
                SavingsGoalMonthlyPaceStatus.NO_ACTIVE_PLAN;
    }

    public boolean hasReachedMonthlyTarget() {
        return status ==
                SavingsGoalMonthlyPaceStatus.MONTHLY_TARGET_REACHED;
    }

    private static void validateNonNegative(
            BigDecimal value,
            String message
    ) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(message);
        }
    }
}