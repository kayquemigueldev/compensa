package com.kayque.compensa.purchase.score.service;

import com.kayque.compensa.purchase.model.PurchaseAdvice;
import com.kayque.compensa.purchase.model.PurchaseDecisionStatus;
import com.kayque.compensa.purchase.score.model.PurchaseScore;
import com.kayque.compensa.purchase.score.model.PurchaseScoreClassification;
import com.kayque.compensa.purchase.score.model.PurchaseScoreFactor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PurchaseScoreAdviceAdapter {

    public PurchaseAdvice adapt(PurchaseScore score) {
        Objects.requireNonNull(
                score,
                "O Score Compensa? é obrigatório."
        );

        return new PurchaseAdvice(
                mapStatus(score.classification()),
                score.value(),
                collectReasons(score)
        );
    }

    private PurchaseDecisionStatus mapStatus(
            PurchaseScoreClassification classification
    ) {
        return switch (classification) {
            case EXCELLENT,
                 GOOD ->
                    PurchaseDecisionStatus.MAKES_SENSE;

            case MODERATE ->
                    PurchaseDecisionStatus.THINK_AGAIN;

            case RISKY,
                 CRITICAL ->
                    PurchaseDecisionStatus
                            .PROBABLY_NOT_WORTH_IT;
        };
    }

    private List<String> collectReasons(
            PurchaseScore score
    ) {
        List<String> reasons = new ArrayList<>();

        score.positiveFactors()
                .stream()
                .map(PurchaseScoreFactor::description)
                .forEach(reasons::add);

        score.negativeFactors()
                .stream()
                .map(PurchaseScoreFactor::description)
                .forEach(reasons::add);

        if (reasons.isEmpty()) {
            reasons.add(score.justification());
        }

        return List.copyOf(reasons);
    }
}