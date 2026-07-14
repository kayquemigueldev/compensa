package com.kayque.compensa.purchase.model;

import java.util.Objects;

public record PurchaseDecision(
        PurchaseAnalysis analysis,
        PurchaseDecisionContext context,
        PurchaseAdvice advice,
        PurchaseDecisionOutcome outcome
) {

    public PurchaseDecision {
        Objects.requireNonNull(
                analysis,
                "A análise da compra é obrigatória."
        );

        Objects.requireNonNull(
                context,
                "O contexto da decisão é obrigatório."
        );

        Objects.requireNonNull(
                advice,
                "A recomendação é obrigatória."
        );

        Objects.requireNonNull(
                outcome,
                "A decisão tomada é obrigatória."
        );
    }
}