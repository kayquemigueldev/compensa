package com.kayque.compensa.profile.service;

import com.kayque.compensa.profile.model.FinancialProfile;
import com.kayque.compensa.profile.model.MonthlyBudgetStatus;
import com.kayque.compensa.profile.model.MonthlyBudgetSummary;

import java.math.BigDecimal;
import java.util.Objects;

public class MonthlyBudgetService {

    public MonthlyBudgetSummary calculate(
            FinancialProfile profile
    ) {
        Objects.requireNonNull(
                profile,
                "O perfil financeiro não pode ser nulo."
        );

        BigDecimal availableAmount =
                profile.netMonthlyIncome()
                        .subtract(profile.essentialExpenses())
                        .subtract(profile.monthlySavingsGoal());

        return new MonthlyBudgetSummary(
                profile.netMonthlyIncome(),
                profile.essentialExpenses(),
                profile.monthlySavingsGoal(),
                availableAmount,
                determineStatus(availableAmount)
        );
    }

    private MonthlyBudgetStatus determineStatus(
            BigDecimal availableAmount
    ) {
        int comparison =
                availableAmount.compareTo(BigDecimal.ZERO);

        if (comparison > 0) {
            return MonthlyBudgetStatus.AVAILABLE;
        }

        if (comparison == 0) {
            return MonthlyBudgetStatus.BALANCED;
        }

        return MonthlyBudgetStatus.DEFICIT;
    }
}