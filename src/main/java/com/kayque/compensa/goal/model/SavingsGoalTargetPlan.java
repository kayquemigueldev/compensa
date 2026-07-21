package com.kayque.compensa.goal.model;

import java.math.BigDecimal;
import java.util.Objects;

public record SavingsGoalTargetPlan(
        SavingsGoalTargetPlanStatus status,
        BigDecimal remainingAmount,
        long monthsAvailable,
        BigDecimal requiredMonthlyAmount
) {

    public SavingsGoalTargetPlan {
        Objects.requireNonNull(
                status,
                "O status do planejamento é obrigatório."
        );

        Objects.requireNonNull(
                remainingAmount,
                "O valor restante é obrigatório."
        );

        Objects.requireNonNull(
                requiredMonthlyAmount,
                "O valor mensal necessário é obrigatório."
        );

        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "O valor restante não pode ser negativo."
            );
        }

        if (monthsAvailable < 0) {
            throw new IllegalArgumentException(
                    "A quantidade de meses não pode ser negativa."
            );
        }

        if (requiredMonthlyAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "O valor mensal não pode ser negativo."
            );
        }
    }

    public boolean hasMonthlyPlan() {
        return status == SavingsGoalTargetPlanStatus.ACTIVE;
    }
}