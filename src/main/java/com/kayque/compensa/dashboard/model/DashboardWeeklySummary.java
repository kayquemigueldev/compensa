package com.kayque.compensa.dashboard.model;

import java.math.BigDecimal;
import java.util.Objects;

public record DashboardWeeklySummary(
        long totalDecisions,
        long purchasedDecisions,
        long declinedDecisions,
        BigDecimal purchasedValue,
        BigDecimal preservedValue,
        long totalRealWorkMinutes
) {

    public DashboardWeeklySummary {
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
                || totalRealWorkMinutes < 0) {
            throw new IllegalArgumentException(
                    "Os indicadores semanais não podem ser negativos."
            );
        }

        if (purchasedValue.signum() < 0
                || preservedValue.signum() < 0) {
            throw new IllegalArgumentException(
                    "Os valores semanais não podem ser negativos."
            );
        }

        if (purchasedDecisions + declinedDecisions
                > totalDecisions) {
            throw new IllegalArgumentException(
                    "As decisões semanais são inconsistentes."
            );
        }
    }

    public boolean hasDecisions() {
        return totalDecisions > 0;
    }
}