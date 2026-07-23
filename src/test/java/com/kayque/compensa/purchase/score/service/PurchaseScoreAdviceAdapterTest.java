package com.kayque.compensa.purchase.score.service;

import com.kayque.compensa.purchase.model.PurchaseAdvice;
import com.kayque.compensa.purchase.model.PurchaseDecisionStatus;
import com.kayque.compensa.purchase.score.model.PurchaseScore;
import com.kayque.compensa.purchase.score.model.PurchaseScoreClassification;
import com.kayque.compensa.purchase.score.model.PurchaseScoreFactor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PurchaseScoreAdviceAdapterTest {

    private final PurchaseScoreAdviceAdapter adapter =
            new PurchaseScoreAdviceAdapter();

    @Test
    void shouldMapExcellentScoreToMakesSense() {
        PurchaseScore score = new PurchaseScore(
                90,
                PurchaseScoreClassification.EXCELLENT,
                List.of(
                        new PurchaseScoreFactor(
                                "A compra foi planejada.",
                                10
                        )
                ),
                List.of(),
                "A decisão apresenta excelente equilíbrio."
        );

        PurchaseAdvice advice = adapter.adapt(score);

        assertEquals(
                PurchaseDecisionStatus.MAKES_SENSE,
                advice.status()
        );

        assertEquals(90, advice.score());

        assertEquals(
                List.of("A compra foi planejada."),
                advice.reasons()
        );
    }

    @Test
    void shouldMapGoodScoreToMakesSense() {
        PurchaseScore score = new PurchaseScore(
                75,
                PurchaseScoreClassification.GOOD,
                List.of(
                        new PurchaseScoreFactor(
                                "O impacto no orçamento é baixo.",
                                10
                        )
                ),
                List.of(
                        new PurchaseScoreFactor(
                                "A compra não foi planejada.",
                                -10
                        )
                ),
                "Os fatores positivos superam os riscos."
        );

        PurchaseAdvice advice = adapter.adapt(score);

        assertEquals(
                PurchaseDecisionStatus.MAKES_SENSE,
                advice.status()
        );

        assertEquals(75, advice.score());
    }

    @Test
    void shouldMapModerateScoreToThinkAgain() {
        PurchaseScore score = new PurchaseScore(
                55,
                PurchaseScoreClassification.MODERATE,
                List.of(
                        new PurchaseScoreFactor(
                                "O tempo de trabalho é baixo.",
                                5
                        )
                ),
                List.of(
                        new PurchaseScoreFactor(
                                "Existe uma alternativa disponível.",
                                -10
                        )
                ),
                "A decisão merece reflexão."
        );

        PurchaseAdvice advice = adapter.adapt(score);

        assertEquals(
                PurchaseDecisionStatus.THINK_AGAIN,
                advice.status()
        );
    }

    @Test
    void shouldMapRiskyScoreToProbablyNotWorthIt() {
        PurchaseScore score = new PurchaseScore(
                35,
                PurchaseScoreClassification.RISKY,
                List.of(),
                List.of(
                        new PurchaseScoreFactor(
                                "O impacto no orçamento é alto.",
                                -10
                        )
                ),
                "A decisão apresenta riscos relevantes."
        );

        PurchaseAdvice advice = adapter.adapt(score);

        assertEquals(
                PurchaseDecisionStatus
                        .PROBABLY_NOT_WORTH_IT,
                advice.status()
        );
    }

    @Test
    void shouldMapCriticalScoreToProbablyNotWorthIt() {
        PurchaseScore score = new PurchaseScore(
                10,
                PurchaseScoreClassification.CRITICAL,
                List.of(),
                List.of(
                        new PurchaseScoreFactor(
                                "A decisão apresenta sinal de impulso.",
                                -20
                        )
                ),
                "A decisão possui fatores de alto impacto."
        );

        PurchaseAdvice advice = adapter.adapt(score);

        assertEquals(
                PurchaseDecisionStatus
                        .PROBABLY_NOT_WORTH_IT,
                advice.status()
        );
    }

    @Test
    void shouldCollectPositiveAndNegativeReasons() {
        PurchaseScore score = new PurchaseScore(
                60,
                PurchaseScoreClassification.MODERATE,
                List.of(
                        new PurchaseScoreFactor(
                                "A compra foi planejada.",
                                10
                        ),
                        new PurchaseScoreFactor(
                                "O impacto no objetivo é baixo.",
                                5
                        )
                ),
                List.of(
                        new PurchaseScoreFactor(
                                "A compra possui frequência mensal.",
                                -5
                        )
                ),
                "A decisão possui pontos positivos e negativos."
        );

        PurchaseAdvice advice = adapter.adapt(score);

        assertEquals(3, advice.reasons().size());

        assertTrue(
                advice.reasons().contains(
                        "A compra foi planejada."
                )
        );

        assertTrue(
                advice.reasons().contains(
                        "O impacto no objetivo é baixo."
                )
        );

        assertTrue(
                advice.reasons().contains(
                        "A compra possui frequência mensal."
                )
        );
    }

    @Test
    void shouldUseJustificationWhenThereAreNoFactors() {
        PurchaseScore score = new PurchaseScore(
                50,
                PurchaseScoreClassification.MODERATE,
                List.of(),
                List.of(),
                "A decisão precisa de uma análise mais cuidadosa."
        );

        PurchaseAdvice advice = adapter.adapt(score);

        assertEquals(
                List.of(
                        "A decisão precisa de uma análise mais cuidadosa."
                ),
                advice.reasons()
        );
    }
}