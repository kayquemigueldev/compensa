package com.kayque.compensa.purchase.service;

import com.kayque.compensa.profile.model.FinancialProfile;
import com.kayque.compensa.purchase.model.Purchase;
import com.kayque.compensa.purchase.model.PurchaseAdvice;
import com.kayque.compensa.purchase.model.PurchaseAnalysis;
import com.kayque.compensa.purchase.model.PurchaseDecisionContext;
import com.kayque.compensa.purchase.model.PurchaseDecisionStatus;
import com.kayque.compensa.purchase.model.PurchaseFrequency;
import com.kayque.compensa.purchase.model.PurchaseMotivation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PurchaseAdviceServiceTest {

    private final PurchaseAnalysisService analysisService =
            new PurchaseAnalysisService();

    private final PurchaseAdviceService adviceService =
            new PurchaseAdviceService();

    private final FinancialProfile profile =
            new FinancialProfile(
                    new BigDecimal("2000.00"),
                    new BigDecimal("160"),
                    new BigDecimal("40")
            );

    @Test
    void shouldRecommendThinkingAgainForUnplannedDesire() {
        PurchaseAnalysis analysis = createAnalysis(
                "Salgadinho",
                "7.99",
                PurchaseFrequency.WEEKLY
        );

        PurchaseDecisionContext context =
                new PurchaseDecisionContext(
                        false,
                        true,
                        false,
                        PurchaseMotivation.DESIRE
                );

        PurchaseAdvice advice =
                adviceService.evaluate(analysis, context);

        assertEquals(
                PurchaseDecisionStatus.THINK_AGAIN,
                advice.status()
        );

        assertEquals(35, advice.score());
    }

    @Test
    void shouldIndicateThatPlannedUrgentNeedMakesSense() {
        PurchaseAnalysis analysis = createAnalysis(
                "Remédio",
                "30.00",
                PurchaseFrequency.ONCE
        );

        PurchaseDecisionContext context =
                new PurchaseDecisionContext(
                        true,
                        false,
                        true,
                        PurchaseMotivation.NEED
                );

        PurchaseAdvice advice =
                adviceService.evaluate(analysis, context);

        assertEquals(
                PurchaseDecisionStatus.MAKES_SENSE,
                advice.status()
        );

        assertEquals(97, advice.score());
    }

    @Test
    void shouldDiscourageHighImpactImpulsePurchase() {
        PurchaseAnalysis analysis = createAnalysis(
                "Compra por impulso",
                "600.00",
                PurchaseFrequency.ONCE
        );

        PurchaseDecisionContext context =
                new PurchaseDecisionContext(
                        false,
                        true,
                        false,
                        PurchaseMotivation.IMPULSE
                );

        PurchaseAdvice advice =
                adviceService.evaluate(analysis, context);

        assertEquals(
                PurchaseDecisionStatus.PROBABLY_NOT_WORTH_IT,
                advice.status()
        );

        assertEquals(0, advice.score());
    }

    private PurchaseAnalysis createAnalysis(
            String name,
            String price,
            PurchaseFrequency frequency
    ) {
        Purchase purchase = new Purchase(
                name,
                new BigDecimal(price),
                frequency
        );

        return analysisService.analyze(
                purchase,
                profile
        );
    }
}