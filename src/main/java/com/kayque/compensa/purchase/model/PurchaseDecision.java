package com.kayque.compensa.purchase.model;

import com.kayque.compensa.purchase.score.model.PurchaseScore;

import java.util.Objects;

public record PurchaseDecision(
        PurchaseAnalysis analysis,
        PurchaseDecisionContext context,
        PurchaseAdvice advice,
        PurchaseScore score,
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
                "A recomendação da compra é obrigatória."
        );

        Objects.requireNonNull(
                outcome,
                "O resultado da decisão é obrigatório."
        );
    }

    /*
     * Mantém compatibilidade com testes e códigos antigos.
     * Decisões novas serão salvas usando o construtor com score.
     */
    public PurchaseDecision(
            PurchaseAnalysis analysis,
            PurchaseDecisionContext context,
            PurchaseAdvice advice,
            PurchaseDecisionOutcome outcome
    ) {
        this(
                analysis,
                context,
                advice,
                null,
                outcome
        );
    }

    public boolean hasScore() {
        return score != null;
    }
}