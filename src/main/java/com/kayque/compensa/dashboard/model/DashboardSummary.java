package com.kayque.compensa.dashboard.model;

import java.math.BigDecimal;

public record DashboardSummary(
        long totalDecisions,
        long purchasedDecisions,
        long declinedDecisions,
        long waitingDecisions,
        BigDecimal preservedValue,
        long totalRealWorkMinutes
) {

    public DashboardSummary {
        if (totalDecisions < 0
                || purchasedDecisions < 0
                || declinedDecisions < 0
                || waitingDecisions < 0
                || totalRealWorkMinutes < 0) {
            throw new IllegalArgumentException(
                    "Os indicadores não podem ser negativos."
            );
        }

        if (preservedValue == null
                || preservedValue.signum() < 0) {
            throw new IllegalArgumentException(
                    "O valor preservado não pode ser negativo."
            );
        }
    }
}