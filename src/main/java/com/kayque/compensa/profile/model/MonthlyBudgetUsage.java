package com.kayque.compensa.profile.model;

import java.math.BigDecimal;
import java.util.Objects;

public record MonthlyBudgetUsage(
        BigDecimal plannedAvailableAmount,
        BigDecimal purchasedAmount,
        BigDecimal currentAvailableAmount,
        MonthlyBudgetStatus status
) {

    public MonthlyBudgetUsage {
        Objects.requireNonNull(
                plannedAvailableAmount,
                "O orçamento planejado é obrigatório."
        );

        Objects.requireNonNull(
                purchasedAmount,
                "O total de compras é obrigatório."
        );

        Objects.requireNonNull(
                currentAvailableAmount,
                "O saldo atual é obrigatório."
        );

        Objects.requireNonNull(
                status,
                "O status do orçamento é obrigatório."
        );

        if (purchasedAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "O total de compras não pode ser negativo."
            );
        }
    }
}