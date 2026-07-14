package com.kayque.compensa.profile.model;

import java.math.BigDecimal;

public record FinancialProfile(
        BigDecimal netMonthlyIncome,
        BigDecimal monthlyWorkHours,
        BigDecimal monthlyAdditionalHours
) {

    public FinancialProfile {
        validatePositive(
                netMonthlyIncome,
                "A renda mensal líquida deve ser maior que zero."
        );

        validatePositive(
                monthlyWorkHours,
                "As horas mensais de trabalho devem ser maiores que zero."
        );

        validateNonNegative(
                monthlyAdditionalHours,
                "As horas adicionais não podem ser negativas."
        );
    }

    public BigDecimal totalCommittedHours() {
        return monthlyWorkHours.add(monthlyAdditionalHours);
    }

    private static void validatePositive(
            BigDecimal value,
            String message
    ) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void validateNonNegative(
            BigDecimal value,
            String message
    ) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(message);
        }
    }
}