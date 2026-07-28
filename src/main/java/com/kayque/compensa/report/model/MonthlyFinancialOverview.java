package com.kayque.compensa.report.model;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Objects;

public record MonthlyFinancialOverview(
        YearMonth selectedMonth,
        MonthlyFinancialReport currentMonth,
        MonthlyFinancialReport previousMonth,
        MonthlySpendingTrend spendingTrend,
        BigDecimal spendingVariationPercentage
) {

    public MonthlyFinancialOverview {
        Objects.requireNonNull(
                selectedMonth,
                "O mês selecionado é obrigatório."
        );

        Objects.requireNonNull(
                currentMonth,
                "O relatório do mês atual é obrigatório."
        );

        Objects.requireNonNull(
                previousMonth,
                "O relatório do mês anterior é obrigatório."
        );

        Objects.requireNonNull(
                spendingTrend,
                "A tendência mensal é obrigatória."
        );

        Objects.requireNonNull(
                spendingVariationPercentage,
                "A variação mensal é obrigatória."
        );

        if (!currentMonth.month().equals(selectedMonth)) {
            throw new IllegalArgumentException(
                    "O relatório atual não pertence ao mês selecionado."
            );
        }

        if (!previousMonth.month().equals(
                selectedMonth.minusMonths(1)
        )) {
            throw new IllegalArgumentException(
                    "O relatório anterior não pertence ao mês esperado."
            );
        }

        if (spendingVariationPercentage.signum() < 0) {
            throw new IllegalArgumentException(
                    "A variação mensal não pode ser negativa."
            );
        }
    }
}