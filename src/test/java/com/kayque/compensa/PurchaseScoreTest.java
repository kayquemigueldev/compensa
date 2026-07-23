package com.kayque.compensa.purchase.score.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PurchaseScoreTest {

    @Test
    void shouldClassifyExcellentDecision() {
        assertEquals(
                PurchaseScoreClassification.EXCELLENT,
                PurchaseScoreClassification.fromScore(85)
        );

        assertEquals(
                PurchaseScoreClassification.EXCELLENT,
                PurchaseScoreClassification.fromScore(100)
        );
    }

    @Test
    void shouldClassifyGoodDecision() {
        assertEquals(
                PurchaseScoreClassification.GOOD,
                PurchaseScoreClassification.fromScore(70)
        );

        assertEquals(
                PurchaseScoreClassification.GOOD,
                PurchaseScoreClassification.fromScore(84)
        );
    }

    @Test
    void shouldClassifyModerateDecision() {
        assertEquals(
                PurchaseScoreClassification.MODERATE,
                PurchaseScoreClassification.fromScore(50)
        );

        assertEquals(
                PurchaseScoreClassification.MODERATE,
                PurchaseScoreClassification.fromScore(69)
        );
    }

    @Test
    void shouldClassifyRiskyDecision() {
        assertEquals(
                PurchaseScoreClassification.RISKY,
                PurchaseScoreClassification.fromScore(30)
        );

        assertEquals(
                PurchaseScoreClassification.RISKY,
                PurchaseScoreClassification.fromScore(49)
        );
    }

    @Test
    void shouldClassifyCriticalDecision() {
        assertEquals(
                PurchaseScoreClassification.CRITICAL,
                PurchaseScoreClassification.fromScore(0)
        );

        assertEquals(
                PurchaseScoreClassification.CRITICAL,
                PurchaseScoreClassification.fromScore(29)
        );
    }

    @Test
    void shouldRejectInvalidScore() {
        assertThrows(
                IllegalArgumentException.class,
                () -> PurchaseScoreClassification.fromScore(-1)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> PurchaseScoreClassification.fromScore(101)
        );
    }

    @Test
    void shouldIdentifyPositiveAndNegativeFactors() {
        PurchaseScoreFactor positiveFactor =
                new PurchaseScoreFactor(
                        "A compra foi planejada.",
                        10
                );

        PurchaseScoreFactor negativeFactor =
                new PurchaseScoreFactor(
                        "A compra não estava planejada.",
                        -10
                );

        assertTrue(positiveFactor.isPositive());
        assertFalse(positiveFactor.isNegative());

        assertTrue(negativeFactor.isNegative());
        assertFalse(negativeFactor.isPositive());
    }

    @Test
    void shouldCreateCompletePurchaseScore() {
        PurchaseScore score = new PurchaseScore(
                75,
                PurchaseScoreClassification.GOOD,

                List.of(
                        new PurchaseScoreFactor(
                                "A compra foi planejada.",
                                10
                        )
                ),

                List.of(
                        new PurchaseScoreFactor(
                                "A compra consome parte do orçamento.",
                                -5
                        )
                ),

                "A compra apresenta mais fatores positivos do que riscos."
        );

        assertEquals(75, score.value());

        assertEquals(
                PurchaseScoreClassification.GOOD,
                score.classification()
        );

        assertEquals(
                PurchaseScoreColor.GREEN,
                score.color()
        );

        assertEquals(
                "Boa decisão",
                score.classificationDescription()
        );

        assertEquals(1, score.positiveFactors().size());
        assertEquals(1, score.negativeFactors().size());
    }

    @Test
    void shouldRejectIncompatibleClassification() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PurchaseScore(
                        20,
                        PurchaseScoreClassification.EXCELLENT,
                        List.of(),
                        List.of(),
                        "Classificação incorreta."
                )
        );
    }

    @Test
    void shouldRejectFactorsInWrongLists() {
        PurchaseScoreFactor negativeFactor =
                new PurchaseScoreFactor(
                        "A compra não foi planejada.",
                        -10
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> new PurchaseScore(
                        60,
                        PurchaseScoreClassification.MODERATE,
                        List.of(negativeFactor),
                        List.of(),
                        "Existem fatores que precisam ser avaliados."
                )
        );
    }

    @Test
    void shouldProtectFactorListsAgainstModification() {
        List<PurchaseScoreFactor> positiveFactors =
                new ArrayList<>();

        positiveFactors.add(
                new PurchaseScoreFactor(
                        "A compra atende a uma necessidade.",
                        15
                )
        );

        PurchaseScore score = new PurchaseScore(
                80,
                PurchaseScoreClassification.GOOD,
                positiveFactors,
                List.of(),
                "A compra apresenta bons indicadores."
        );

        positiveFactors.clear();

        assertEquals(
                1,
                score.positiveFactors().size()
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> score.positiveFactors().clear()
        );
    }
}