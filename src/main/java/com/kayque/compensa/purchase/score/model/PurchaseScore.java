package com.kayque.compensa.purchase.score.model;

import java.util.List;
import java.util.Objects;

public record PurchaseScore(
        int value,
        PurchaseScoreClassification classification,
        List<PurchaseScoreFactor> positiveFactors,
        List<PurchaseScoreFactor> negativeFactors,
        String justification
) {

    public PurchaseScore {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException(
                    "O Score Compensa? deve estar entre 0 e 100."
            );
        }

        Objects.requireNonNull(
                classification,
                "A classificação do Score Compensa? é obrigatória."
        );

        Objects.requireNonNull(
                positiveFactors,
                "Os fatores positivos são obrigatórios."
        );

        Objects.requireNonNull(
                negativeFactors,
                "Os fatores negativos são obrigatórios."
        );

        Objects.requireNonNull(
                justification,
                "A justificativa do Score Compensa? é obrigatória."
        );

        if (
                classification
                        != PurchaseScoreClassification.fromScore(value)
        ) {
            throw new IllegalArgumentException(
                    "A classificação não corresponde ao valor do Score Compensa?."
            );
        }

        if (
                positiveFactors.stream()
                        .anyMatch(factor -> !factor.isPositive())
        ) {
            throw new IllegalArgumentException(
                    "A lista positiva possui um fator negativo."
            );
        }

        if (
                negativeFactors.stream()
                        .anyMatch(factor -> !factor.isNegative())
        ) {
            throw new IllegalArgumentException(
                    "A lista negativa possui um fator positivo."
            );
        }

        justification = justification.trim();

        if (justification.isEmpty()) {
            throw new IllegalArgumentException(
                    "A justificativa não pode estar vazia."
            );
        }

        positiveFactors = List.copyOf(positiveFactors);
        negativeFactors = List.copyOf(negativeFactors);
    }

    public PurchaseScoreColor color() {
        return classification.getColor();
    }

    public String classificationDescription() {
        return classification.getDescription();
    }
}