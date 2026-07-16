package com.kayque.compensa.profile.service;

import com.kayque.compensa.profile.model.MonthlyBudgetStatus;
import com.kayque.compensa.profile.model.MonthlyBudgetSummary;
import com.kayque.compensa.profile.model.MonthlyBudgetUsage;

import java.math.BigDecimal;
import java.util.Objects;

public class MonthlyBudgetUsageService {

    public MonthlyBudgetUsage calculate(
            MonthlyBudgetSummary plannedBudget,
            BigDecimal purchasedAmount
    ) {
        Objects.requireNonNull(
                plannedBudget,
                "O orçamento planejado não pode ser nulo."
        );

        Objects.requireNonNull(
                purchasedAmount,
                "O total de compras não pode ser nulo."
        );

        if (purchasedAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "O total de compras não pode ser negativo."
            );
        }

        BigDecimal currentAvailableAmount =
                plannedBudget.availableAmount()
                        .subtract(purchasedAmount);

        return new MonthlyBudgetUsage(
                plannedBudget.availableAmount(),
                purchasedAmount,
                currentAvailableAmount,
                determineStatus(currentAvailableAmount)
        );
    }

    private MonthlyBudgetStatus determineStatus(
            BigDecimal currentAvailableAmount
    ) {
        int comparison =
                currentAvailableAmount.compareTo(BigDecimal.ZERO);

        if (comparison > 0) {
            return MonthlyBudgetStatus.AVAILABLE;
        }

        if (comparison == 0) {
            return MonthlyBudgetStatus.BALANCED;
        }

        return MonthlyBudgetStatus.DEFICIT;
    }
}