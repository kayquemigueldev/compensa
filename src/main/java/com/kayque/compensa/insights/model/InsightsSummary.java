package com.kayque.compensa.insights.model;

public record InsightsSummary(
        long totalDecisions,
        long purchasedDecisions,
        long declinedDecisions,
        long waitingDecisions,
        long purchasesAgainstAdvice,
        long totalRealWorkMinutes
) {

    public InsightsSummary {
        if (totalDecisions < 0
                || purchasedDecisions < 0
                || declinedDecisions < 0
                || waitingDecisions < 0
                || purchasesAgainstAdvice < 0
                || totalRealWorkMinutes < 0) {
            throw new IllegalArgumentException(
                    "Os dados dos insights não podem ser negativos."
            );
        }
    }
}