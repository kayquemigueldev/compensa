package com.kayque.compensa.insights.model;

public record InsightsSummary(
        long totalDecisions,
        long purchasedDecisions,
        long declinedDecisions,
        long waitingDecisions,
        long purchasesAgainstAdvice,
        long totalRealWorkMinutes,
        long evaluatedPurchases,
        long worthItPurchases,
        long partiallyWorthItPurchases,
        long regrettedPurchases
) {

    public InsightsSummary {
        if (totalDecisions < 0
                || purchasedDecisions < 0
                || declinedDecisions < 0
                || waitingDecisions < 0
                || purchasesAgainstAdvice < 0
                || totalRealWorkMinutes < 0
                || evaluatedPurchases < 0
                || worthItPurchases < 0
                || partiallyWorthItPurchases < 0
                || regrettedPurchases < 0) {

            throw new IllegalArgumentException(
                    "Os dados dos insights não podem ser negativos."
            );
        }

        long satisfactionTotal =
                worthItPurchases
                        + partiallyWorthItPurchases
                        + regrettedPurchases;

        if (satisfactionTotal != evaluatedPurchases) {
            throw new IllegalArgumentException(
                    "O total das avaliações está inconsistente."
            );
        }

        if (evaluatedPurchases > purchasedDecisions) {
            throw new IllegalArgumentException(
                    "Não pode haver mais avaliações que compras."
            );
        }
    }

    public InsightsSummary(
            long totalDecisions,
            long purchasedDecisions,
            long declinedDecisions,
            long waitingDecisions,
            long purchasesAgainstAdvice,
            long totalRealWorkMinutes
    ) {
        this(
                totalDecisions,
                purchasedDecisions,
                declinedDecisions,
                waitingDecisions,
                purchasesAgainstAdvice,
                totalRealWorkMinutes,
                0,
                0,
                0,
                0
        );
    }
}