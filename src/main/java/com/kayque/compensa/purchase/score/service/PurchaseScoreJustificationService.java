package com.kayque.compensa.purchase.score.service;

import com.kayque.compensa.purchase.score.model.PurchaseScoreClassification;

import java.util.Objects;

public class PurchaseScoreJustificationService {

    public String create(
            PurchaseScoreClassification classification
    ) {
        Objects.requireNonNull(
                classification,
                "A classificação do Score Compensa? é obrigatória."
        );

        return switch (classification) {
            case EXCELLENT ->
                    "A decisão apresenta excelente equilíbrio entre necessidade, planejamento e impacto financeiro.";

            case GOOD ->
                    "A decisão possui mais fatores positivos do que riscos financeiros.";

            case MODERATE ->
                    "A compra é possível, mas alguns fatores merecem reflexão antes da decisão.";

            case RISKY ->
                    "A decisão apresenta riscos relevantes para o orçamento ou para seus objetivos.";

            case CRITICAL ->
                    "A compra reúne fatores de alto impacto e merece ser reconsiderada com cuidado.";
        };
    }
}