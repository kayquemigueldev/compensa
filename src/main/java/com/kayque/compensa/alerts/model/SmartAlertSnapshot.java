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
        int unevaluatedPurchases,
        long totalWorkMinutes,
        BigDecimal preservedAmountThisYear,
        boolean financialProfileConfigured
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

        requireNonNegative(
                purchasesMade,
                "A quantidade de compras realizadas não pode ser negativa."
        );

        requireNonNegative(
                purchasesAvoided,
                "A quantidade de compras evitadas não pode ser negativa."
        );

        requireNonNegative(
                pendingDecisions,
                "A quantidade de decisões pendentes não pode ser negativa."
        );

        requireNonNegative(
                overduePendingDecisions,
                "A quantidade de decisões atrasadas não pode ser negativa."
        );

        if (overduePendingDecisions > pendingDecisions) {
            throw new IllegalArgumentException(
                    "As decisões atrasadas não podem superar o total de decisões pendentes."
            );
        }

        requireNonNegative(
                unevaluatedPurchases,
                "A quantidade de compras sem avaliação não pode ser negativa."
        );

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

    /*
     * Mantém compatibilidade com os testes e códigos
     * criados antes da informação do perfil financeiro.
     */
    public SmartAlertSnapshot(
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
            int unevaluatedPurchases,
            long totalWorkMinutes,
            BigDecimal preservedAmountThisYear
    ) {
        this(
                budgetUsagePercentage,
                availableBudget,
                monthlyGoalContributions,
                monthlyGoalTarget,
                goalProgressPercentage,
                expectedGoalProgressPercentage,
                purchasesMade,
                purchasesAvoided,
                pendingDecisions,
                overduePendingDecisions,
                unevaluatedPurchases,
                totalWorkMinutes,
                preservedAmountThisYear,
                true
        );
    }

    public SmartAlertSnapshot(
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
        this(
                budgetUsagePercentage,
                availableBudget,
                monthlyGoalContributions,
                monthlyGoalTarget,
                goalProgressPercentage,
                expectedGoalProgressPercentage,
                purchasesMade,
                purchasesAvoided,
                pendingDecisions,
                overduePendingDecisions,
                0,
                totalWorkMinutes,
                preservedAmountThisYear,
                true
        );
    }

    public boolean hasUnevaluatedPurchases() {
        return unevaluatedPurchases > 0;
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

    private static void requireNonNegative(
            int value,
            String message
    ) {
        if (value < 0) {
            throw new IllegalArgumentException(message);
        }
    }
}