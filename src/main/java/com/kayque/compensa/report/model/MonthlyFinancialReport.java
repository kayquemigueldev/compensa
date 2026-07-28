package com.kayque.compensa.report.model;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Objects;

public record MonthlyFinancialReport(
        YearMonth month,
        long totalDecisions,
        long purchasedDecisions,
        long declinedDecisions,
        long waitingDecisions,
        BigDecimal purchasedValue,
        BigDecimal preservedValue,
        long totalRealWorkMinutes
) {

    public MonthlyFinancialReport {
        Objects.requireNonNull(
                month,
                "O mês do relatório é obrigatório."
        );

        Objects.requireNonNull(
                purchasedValue,
                "O valor comprado é obrigatório."
        );

        Objects.requireNonNull(
                preservedValue,
                "O valor preservado é obrigatório."
        );

        if (totalDecisions < 0
                || purchasedDecisions < 0
                || declinedDecisions < 0
                || waitingDecisions < 0
                || totalRealWorkMinutes < 0) {
            throw new IllegalArgumentException(
                    "Os indicadores mensais não podem ser negativos."
            );
        }

        if (purchasedValue.signum() < 0
                || preservedValue.signum() < 0) {
            throw new IllegalArgumentException(
                    "Os valores mensais não podem ser negativos."
            );
        }

        long classifiedDecisions =
                purchasedDecisions
                        + declinedDecisions
                        + waitingDecisions;

        if (classifiedDecisions > totalDecisions) {
            throw new IllegalArgumentException(
                    "As decisões mensais são inconsistentes."
            );
        }
    }

    public boolean hasDecisions() {
        return totalDecisions > 0;
    }

    public static MonthlyFinancialReport empty(
            YearMonth month
    ) {
        return new MonthlyFinancialReport(
                month,
                0,
                0,
                0,
                0,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0
        );
    }
}