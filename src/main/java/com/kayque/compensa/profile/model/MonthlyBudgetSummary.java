package com.kayque.compensa.profile.model;

import java.math.BigDecimal;
import java.util.Objects;

public record MonthlyBudgetSummary(
        BigDecimal netMonthlyIncome,
        BigDecimal essentialExpenses,
        BigDecimal monthlySavingsGoal,
        BigDecimal availableAmount,
        MonthlyBudgetStatus status
) {

    public MonthlyBudgetSummary {
        Objects.requireNonNull(
                netMonthlyIncome,
                "A renda mensal é obrigatória."
        );

        Objects.requireNonNull(
                essentialExpenses,
                "As despesas essenciais são obrigatórias."
        );

        Objects.requireNonNull(
                monthlySavingsGoal,
                "A meta de economia é obrigatória."
        );

        Objects.requireNonNull(
                availableAmount,
                "O valor disponível é obrigatório."
        );

        Objects.requireNonNull(
                status,
                "O status do orçamento é obrigatório."
        );
    }
}