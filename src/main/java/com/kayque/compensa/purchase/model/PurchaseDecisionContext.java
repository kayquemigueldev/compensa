package com.kayque.compensa.purchase.model;

import java.util.Objects;

public record PurchaseDecisionContext(
        boolean planned,
        boolean hasAlternative,
        boolean urgent,
        PurchaseMotivation motivation
) {

    public PurchaseDecisionContext {
        Objects.requireNonNull(
                motivation,
                "A motivação da compra é obrigatória."
        );
    }
}