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
import com.kayque.compensa.purchase.model.PurchaseBudgetImpact;
import com.kayque.compensa.purchase.model.PurchaseBudgetImpactStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void shouldConsiderLowBudgetImpactWithoutPenalty() {
        PurchaseAnalysis analysis = createAnalysis(
                "Salgadinho",
                "7.99",
                PurchaseFrequency.ONCE
        );

        PurchaseDecisionContext context =
                new PurchaseDecisionContext(
                        false,
                        true,
                        false,
                        PurchaseMotivation.DESIRE
                );

        PurchaseBudgetImpact impact =
                new PurchaseBudgetImpact(
                        new BigDecimal("500.00"),
                        new BigDecimal("492.01"),
                        Optional.of(
                                new BigDecimal("1.60")
                        ),
                        PurchaseBudgetImpactStatus.WITHIN_BUDGET
                );

        PurchaseAdvice advice =
                adviceService.evaluate(
                        analysis,
                        context,
                        impact
                );

        assertEquals(35, advice.score());

        assertTrue(
                advice.reasons().contains(
                        "A compra preserva a maior parte do dinheiro livre do mês."
                )
        );
    }

    @Test
    void shouldWarnUrgentNeedWhenBudgetIsInDeficit() {
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

        PurchaseBudgetImpact impact =
                new PurchaseBudgetImpact(
                        new BigDecimal("-200.00"),
                        new BigDecimal("-230.00"),
                        Optional.empty(),
                        PurchaseBudgetImpactStatus
                                .BUDGET_IN_DEFICIT
                );

        PurchaseAdvice advice =
                adviceService.evaluate(
                        analysis,
                        context,
                        impact
                );

        assertEquals(
                PurchaseDecisionStatus.THINK_AGAIN,
                advice.status()
        );

        assertEquals(67, advice.score());

        assertTrue(
                advice.reasons().contains(
                        "O orçamento mensal já está em déficit."
                )
        );
    }

    @Test
    void shouldPenalizePurchaseAboveAvailableAmount() {
        PurchaseAnalysis analysis = createAnalysis(
                "Compra acima do orçamento",
                "600.00",
                PurchaseFrequency.ONCE
        );

        PurchaseDecisionContext context =
                new PurchaseDecisionContext(
                        true,
                        false,
                        false,
                        PurchaseMotivation.NEED
                );

        PurchaseBudgetImpact impact =
                new PurchaseBudgetImpact(
                        new BigDecimal("500.00"),
                        new BigDecimal("-100.00"),
                        Optional.of(
                                new BigDecimal("120.00")
                        ),
                        PurchaseBudgetImpactStatus
                                .EXCEEDS_AVAILABLE_AMOUNT
                );

        PurchaseAdvice advice =
                adviceService.evaluate(
                        analysis,
                        context,
                        impact
                );

        assertEquals(
                PurchaseDecisionStatus.THINK_AGAIN,
                advice.status()
        );

        assertEquals(50, advice.score());

        assertTrue(
                advice.reasons().contains(
                        "A compra ultrapassa o dinheiro livre deste mês."
                )
        );
    }

}