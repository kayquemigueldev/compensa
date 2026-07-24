package com.kayque.compensa.alerts.model;

import java.math.BigDecimal;
import java.util.Objects;

public record SmartAlertDecisionMetrics(
        int purchasesMadeThisMonth,
        int purchasesAvoidedThisMonth,
        int pendingDecisions,
        int overduePendingDecisions,
        int unevaluatedPurchases,
        long workMinutesThisMonth,
        BigDecimal preservedAmountThisYear
) {

    public SmartAlertDecisionMetrics {
        requireNonNegative(
                purchasesMadeThisMonth,
                "A quantidade de compras realizadas não pode ser negativa."
        );

        requireNonNegative(
                purchasesAvoidedThisMonth,
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

        requireNonNegative(
                unevaluatedPurchases,
                "A quantidade de compras sem avaliação não pode ser negativa."
        );

        if (overduePendingDecisions > pendingDecisions) {
            throw new IllegalArgumentException(
                    "As decisões atrasadas não podem superar o total de decisões pendentes."
            );
        }

        if (workMinutesThisMonth < 0) {
            throw new IllegalArgumentException(
                    "O tempo de trabalho não pode ser negativo."
            );
        }

        Objects.requireNonNull(
                preservedAmountThisYear,
                "O valor preservado é obrigatório."
        );

        if (preservedAmountThisYear.signum() < 0) {
            throw new IllegalArgumentException(
                    "O valor preservado não pode ser negativo."
            );
        }
    }

    /*
     * Mantém compatibilidade com códigos e testes anteriores.
     */
    public SmartAlertDecisionMetrics(
            int purchasesMadeThisMonth,
            int purchasesAvoidedThisMonth,
            int pendingDecisions,
            int overduePendingDecisions,
            long workMinutesThisMonth,
            BigDecimal preservedAmountThisYear
    ) {
        this(
                purchasesMadeThisMonth,
                purchasesAvoidedThisMonth,
                pendingDecisions,
                overduePendingDecisions,
                0,
                workMinutesThisMonth,
                preservedAmountThisYear
        );
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