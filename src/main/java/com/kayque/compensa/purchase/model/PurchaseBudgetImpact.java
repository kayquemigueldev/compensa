package com.kayque.compensa.purchase.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public record PurchaseBudgetImpact(
        BigDecimal availableAmount,
        BigDecimal remainingAfterPurchase,
        Optional<BigDecimal> budgetUsagePercentage,
        PurchaseBudgetImpactStatus status
) {

    public PurchaseBudgetImpact {
        Objects.requireNonNull(
                availableAmount,
                "O orçamento disponível é obrigatório."
        );

        Objects.requireNonNull(
                remainingAfterPurchase,
                "O saldo após a compra é obrigatório."
        );

        Objects.requireNonNull(
                budgetUsagePercentage,
                "A porcentagem do orçamento é obrigatória."
        );

        Objects.requireNonNull(
                status,
                "O status do impacto é obrigatório."
        );

        budgetUsagePercentage.ifPresent(percentage -> {
            if (percentage.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "A porcentagem não pode ser negativa."
                );
            }
        });
    }
}