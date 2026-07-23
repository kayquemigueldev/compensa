package com.kayque.compensa.purchase.score.service;

import com.kayque.compensa.purchase.model.PurchaseFrequency;
import com.kayque.compensa.purchase.model.PurchaseMotivation;
import com.kayque.compensa.purchase.score.model.PurchaseScore;
import com.kayque.compensa.purchase.score.model.PurchaseScoreClassification;
import com.kayque.compensa.purchase.score.model.PurchaseScoreContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PurchaseScoreServiceTest {

    private final PurchaseScoreService service =
            new PurchaseScoreService();

    @Test
    void shouldGenerateExcellentScore() {
        PurchaseScore score = service.calculate(
                context(
                        PurchaseMotivation.NEED,
                        PurchaseFrequency.ONCE,
                        true,
                        true,
                        false,
                        30,
                        40,
                        "200",
                        "5000",
                        "5",
                        "1"
                )
        );

        assertEquals(100, score.value());

        assertEquals(
                PurchaseScoreClassification.EXCELLENT,
                score.classification()
        );

        assertFalse(score.positiveFactors().isEmpty());
        assertTrue(score.negativeFactors().isEmpty());
    }

    @Test
    void shouldGenerateCriticalScore() {
        PurchaseScore score = service.calculate(
                context(
                        PurchaseMotivation.IMPULSE,
                        PurchaseFrequency.DAILY,
                        false,
                        false,
                        true,
                        480,
                        720,
                        "30000",
                        "3000",
                        "80",
                        "30"
                )
        );

        assertEquals(0, score.value());

        assertEquals(
                PurchaseScoreClassification.CRITICAL,
                score.classification()
        );

        assertTrue(score.positiveFactors().isEmpty());
        assertFalse(score.negativeFactors().isEmpty());
    }

    @Test
    void shouldEvaluateRecurringFrequency() {
        PurchaseScore score = service.calculate(
                context(
                        PurchaseMotivation.DESIRE,
                        PurchaseFrequency.WEEKLY,
                        true,
                        false,
                        false,
                        60,
                        60,
                        "1200",
                        "4000",
                        null,
                        null
                )
        );

        assertTrue(
                score.negativeFactors()
                        .stream()
                        .anyMatch(
                                factor ->
                                        factor.description()
                                                .contains(
                                                        "frequência semanal"
                                                )
                        )
        );
    }

    @Test
    void shouldEvaluateGoalImpact() {
        PurchaseScore score = service.calculate(
                context(
                        PurchaseMotivation.DESIRE,
                        PurchaseFrequency.ONCE,
                        true,
                        false,
                        false,
                        60,
                        60,
                        "500",
                        "4000",
                        "5",
                        "30"
                )
        );

        assertTrue(
                score.negativeFactors()
                        .stream()
                        .anyMatch(
                                factor ->
                                        factor.description()
                                                .contains(
                                                        "um quarto do objetivo"
                                                )
                        )
        );
    }

    @Test
    void shouldEvaluateAdditionalCommittedTime() {
        PurchaseScore score = service.calculate(
                context(
                        PurchaseMotivation.DESIRE,
                        PurchaseFrequency.ONCE,
                        true,
                        false,
                        false,
                        120,
                        300,
                        "500",
                        "4000",
                        null,
                        null
                )
        );

        assertTrue(
                score.negativeFactors()
                        .stream()
                        .anyMatch(
                                factor ->
                                        factor.description()
                                                .contains(
                                                        "tempo adicional"
                                                )
                        )
        );
    }

    @Test
    void shouldSupportMissingBudgetAndGoal() {
        PurchaseScore score = service.calculate(
                context(
                        PurchaseMotivation.DESIRE,
                        PurchaseFrequency.ONCE,
                        true,
                        false,
                        false,
                        90,
                        90,
                        "100",
                        "4000",
                        null,
                        null
                )
        );

        assertEquals(
                PurchaseScoreClassification.GOOD,
                score.classification()
        );
    }

    @Test
    void shouldNormalizeScoreToMaximumOneHundred() {
        PurchaseScore score = service.calculate(
                context(
                        PurchaseMotivation.NEED,
                        PurchaseFrequency.ONCE,
                        true,
                        true,
                        false,
                        10,
                        10,
                        "10",
                        "10000",
                        "1",
                        "1"
                )
        );

        assertEquals(100, score.value());
    }

    private PurchaseScoreContext context(
            PurchaseMotivation motivation,
            PurchaseFrequency frequency,
            boolean planned,
            boolean urgent,
            boolean hasAlternative,
            long professionalMinutes,
            long realMinutes,
            String annualCost,
            String monthlyIncome,
            String budgetImpact,
            String goalImpact
    ) {
        return new PurchaseScoreContext(
                motivation,
                frequency,
                planned,
                urgent,
                hasAlternative,
                professionalMinutes,
                realMinutes,
                new BigDecimal(annualCost),
                new BigDecimal(monthlyIncome),
                decimalOrNull(budgetImpact),
                decimalOrNull(goalImpact)
        );
    }

    private BigDecimal decimalOrNull(String value) {
        if (value == null) {
            return null;
        }

        return new BigDecimal(value);
    }
}