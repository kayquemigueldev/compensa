package com.kayque.compensa.dashboard.model;

public record PurchaseAttentionSummary(
        long waitingDecisions,
        long unevaluatedPurchases
) {

    public PurchaseAttentionSummary {
        if (waitingDecisions < 0) {
            throw new IllegalArgumentException(
                    "A quantidade de decisões aguardando não pode ser negativa."
            );
        }

        if (unevaluatedPurchases < 0) {
            throw new IllegalArgumentException(
                    "A quantidade de compras sem avaliação não pode ser negativa."
            );
        }
    }

    public boolean hasPendingItems() {
        return waitingDecisions > 0
                || unevaluatedPurchases > 0;
    }
}