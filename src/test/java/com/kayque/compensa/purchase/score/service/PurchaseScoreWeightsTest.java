package com.kayque.compensa.purchase.score.service;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PurchaseScoreWeightsTest {

    @Test
    void shouldProvideDefaultWeights() {
        PurchaseScoreWeights weights =
                PurchaseScoreWeights.defaultWeights();

        assertEquals(
                10,
                weights.pointsFor(
                        PurchaseScoreCriterion.PLANNED_PURCHASE
                )
        );

        assertEquals(
                -20,
                weights.pointsFor(
                        PurchaseScoreCriterion.IMPULSE_MOTIVATION
                )
        );

        assertEquals(
                -25,
                weights.pointsFor(
                        PurchaseScoreCriterion.CRITICAL_BUDGET_IMPACT
                )
        );
    }

    @Test
    void shouldContainEveryCriterion() {
        PurchaseScoreWeights weights =
                PurchaseScoreWeights.defaultWeights();

        assertEquals(
                PurchaseScoreCriterion.values().length,
                weights.asMap().size()
        );
    }

    @Test
    void shouldRejectMissingCriterion() {
        Map<PurchaseScoreCriterion, Integer> incompleteWeights =
                new EnumMap<>(
                        PurchaseScoreCriterion.class
                );

        incompleteWeights.put(
                PurchaseScoreCriterion.PLANNED_PURCHASE,
                10
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new PurchaseScoreWeights(
                        incompleteWeights
                )
        );
    }

    @Test
    void shouldAllowCustomizedWeights() {
        EnumMap<PurchaseScoreCriterion, Integer> customized =
                new EnumMap<>(
                        PurchaseScoreCriterion.class
                );

        for (
                PurchaseScoreCriterion criterion
                : PurchaseScoreCriterion.values()
        ) {
            customized.put(criterion, 1);
        }

        customized.put(
                PurchaseScoreCriterion.IMPULSE_MOTIVATION,
                -35
        );

        PurchaseScoreWeights weights =
                new PurchaseScoreWeights(customized);

        assertEquals(
                -35,
                weights.pointsFor(
                        PurchaseScoreCriterion.IMPULSE_MOTIVATION
                )
        );
    }

    @Test
    void shouldProtectWeightsAgainstModification() {
        PurchaseScoreWeights weights =
                PurchaseScoreWeights.defaultWeights();

        assertThrows(
                UnsupportedOperationException.class,
                () -> weights.asMap().put(
                        PurchaseScoreCriterion.PLANNED_PURCHASE,
                        50
                )
        );
    }
}