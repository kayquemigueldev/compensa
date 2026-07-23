package com.kayque.compensa.purchase.score.service;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class PurchaseScoreWeights {

    private final Map<PurchaseScoreCriterion, Integer> weights;

    public PurchaseScoreWeights(
            Map<PurchaseScoreCriterion, Integer> weights
    ) {
        Objects.requireNonNull(
                weights,
                "Os pesos do Score Compensa? são obrigatórios."
        );

        EnumMap<PurchaseScoreCriterion, Integer> copiedWeights =
                new EnumMap<>(
                        PurchaseScoreCriterion.class
                );

        copiedWeights.putAll(weights);

        validateAllCriteria(copiedWeights);

        this.weights = Map.copyOf(copiedWeights);
    }

    public int pointsFor(
            PurchaseScoreCriterion criterion
    ) {
        Objects.requireNonNull(
                criterion,
                "O critério do Score Compensa? é obrigatório."
        );

        return weights.get(criterion);
    }

    public Map<PurchaseScoreCriterion, Integer> asMap() {
        return weights;
    }

    public static PurchaseScoreWeights defaultWeights() {
        EnumMap<PurchaseScoreCriterion, Integer> weights =
                new EnumMap<>(
                        PurchaseScoreCriterion.class
                );

        weights.put(
                PurchaseScoreCriterion.PLANNED_PURCHASE,
                10
        );

        weights.put(
                PurchaseScoreCriterion.UNPLANNED_PURCHASE,
                -10
        );

        weights.put(
                PurchaseScoreCriterion.NECESSITY_MOTIVATION,
                15
        );

        weights.put(
                PurchaseScoreCriterion.IMPULSE_MOTIVATION,
                -20
        );

        weights.put(
                PurchaseScoreCriterion.URGENT_PURCHASE,
                5
        );

        weights.put(
                PurchaseScoreCriterion.HAS_NO_ALTERNATIVE,
                5
        );

        weights.put(
                PurchaseScoreCriterion.HAS_ALTERNATIVE,
                -10
        );

        weights.put(
                PurchaseScoreCriterion.LOW_BUDGET_IMPACT,
                10
        );

        weights.put(
                PurchaseScoreCriterion.MODERATE_BUDGET_IMPACT,
                5
        );

        weights.put(
                PurchaseScoreCriterion.HIGH_BUDGET_IMPACT,
                -10
        );

        weights.put(
                PurchaseScoreCriterion.CRITICAL_BUDGET_IMPACT,
                -25
        );

        weights.put(
                PurchaseScoreCriterion.LOW_WORK_TIME,
                5
        );

        weights.put(
                PurchaseScoreCriterion.HIGH_WORK_TIME,
                -10
        );

        weights.put(
                PurchaseScoreCriterion.CRITICAL_WORK_TIME,
                -20
        );

        weights.put(
                PurchaseScoreCriterion.HIGH_ADDITIONAL_TIME,
                -5
        );

        weights.put(
                PurchaseScoreCriterion.LOW_GOAL_IMPACT,
                5
        );

        weights.put(
                PurchaseScoreCriterion.HIGH_GOAL_IMPACT,
                -10
        );

        weights.put(
                PurchaseScoreCriterion.CRITICAL_GOAL_IMPACT,
                -20
        );

        weights.put(
                PurchaseScoreCriterion.MONTHLY_FREQUENCY,
                -5
        );

        weights.put(
                PurchaseScoreCriterion.WEEKLY_FREQUENCY,
                -12
        );

        weights.put(
                PurchaseScoreCriterion.DAILY_FREQUENCY,
                -20
        );

        weights.put(
                PurchaseScoreCriterion.HIGH_ANNUAL_PROJECTION,
                -10
        );

        weights.put(
                PurchaseScoreCriterion.CRITICAL_ANNUAL_PROJECTION,
                -20
        );

        return new PurchaseScoreWeights(weights);
    }

    private void validateAllCriteria(
            Map<PurchaseScoreCriterion, Integer> weights
    ) {
        for (
                PurchaseScoreCriterion criterion
                : PurchaseScoreCriterion.values()
        ) {
            if (!weights.containsKey(criterion)) {
                throw new IllegalArgumentException(
                        "Não foi definido um peso para o critério "
                                + criterion
                                + "."
                );
            }

            Integer points = weights.get(criterion);

            if (points == null) {
                throw new IllegalArgumentException(
                        "O peso do critério "
                                + criterion
                                + " não pode ser nulo."
                );
            }

            if (points == 0) {
                throw new IllegalArgumentException(
                        "O peso do critério "
                                + criterion
                                + " não pode ser zero."
                );
            }

            if (points < -100 || points > 100) {
                throw new IllegalArgumentException(
                        "O peso do critério "
                                + criterion
                                + " deve estar entre -100 e 100."
                );
            }
        }
    }
}