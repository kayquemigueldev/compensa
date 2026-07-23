package com.kayque.compensa.alerts.model;

import java.math.BigDecimal;
import java.util.Objects;

public record SmartAlertSnapshot(
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

    public SmartAlertSnapshot {
        requireNonNegative(
                budgetUsagePercentage,
                "O percentual utilizado do orçamento não pode ser negativo."
        );

        Objects.requireNonNull(
                availableBudget,
                "O valor disponível no orçamento é obrigatório."
        );

        requireNonNegative(
                monthlyGoalContributions,
                "As contribuições mensais não podem ser negativas."
        );

        requireNonNegative(
                monthlyGoalTarget,
                "A meta mensal não pode ser negativa."
        );

        requireNonNegative(
                goalProgressPercentage,
                "O progresso do objetivo não pode ser negativo."
        );

        requireNonNegative(
                expectedGoalProgressPercentage,
                "O progresso esperado não pode ser negativo."
        );

        if (purchasesMade < 0) {
            throw new IllegalArgumentException(
                    "A quantidade de compras realizadas não pode ser negativa."
            );
        }

        if (purchasesAvoided < 0) {
            throw new IllegalArgumentException(
                    "A quantidade de compras evitadas não pode ser negativa."
            );
        }

        if (pendingDecisions < 0) {
            throw new IllegalArgumentException(
                    "A quantidade de decisões pendentes não pode ser negativa."
            );
        }

        if (overduePendingDecisions < 0) {
            throw new IllegalArgumentException(
                    "A quantidade de decisões atrasadas não pode ser negativa."
            );
        }

        if (overduePendingDecisions > pendingDecisions) {
            throw new IllegalArgumentException(
                    "As decisões atrasadas não podem superar o total de decisões pendentes."
            );
        }

        if (totalWorkMinutes < 0) {
            throw new IllegalArgumentException(
                    "O tempo total de trabalho não pode ser negativo."
            );
        }

        requireNonNegative(
                preservedAmountThisYear,
                "O valor preservado no ano não pode ser negativo."
        );
    }

    public boolean hasMonthlyGoal() {
        return monthlyGoalTarget.signum() > 0;
    }

    public boolean hasFinancialGoal() {
        return goalProgressPercentage.signum() > 0
                || expectedGoalProgressPercentage.signum() > 0;
    }

    public boolean hasPendingDecisions() {
        return pendingDecisions > 0;
    }

    public boolean hasOverduePendingDecisions() {
        return overduePendingDecisions > 0;
    }

    private static void requireNonNegative(
            BigDecimal value,
            String message
    ) {
        Objects.requireNonNull(value, message);

        if (value.signum() < 0) {
            throw new IllegalArgumentException(message);
        }
    }
}