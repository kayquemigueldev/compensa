package com.kayque.compensa.purchase.score.service;

import com.kayque.compensa.profile.model.FinancialProfile;
import com.kayque.compensa.purchase.model.Purchase;
import com.kayque.compensa.purchase.model.PurchaseAnalysis;
import com.kayque.compensa.purchase.model.PurchaseBudgetImpact;
import com.kayque.compensa.purchase.model.PurchaseBudgetImpactStatus;
import com.kayque.compensa.purchase.model.PurchaseDecisionContext;
import com.kayque.compensa.purchase.model.PurchaseFrequency;
import com.kayque.compensa.purchase.model.PurchaseMotivation;
import com.kayque.compensa.purchase.score.model.PurchaseScoreContext;
import com.kayque.compensa.userprofile.model.PurchaseDreamImpact;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PurchaseScoreContextFactoryTest {

    private final PurchaseScoreContextFactory factory =
            new PurchaseScoreContextFactory();

    @Test
    void shouldCreateCompleteScoreContext() {
        Purchase purchase = new Purchase(
                "Notebook",
                new BigDecimal("3000"),
                PurchaseFrequency.ONCE
        );

        PurchaseAnalysis analysis = new PurchaseAnalysis(
                purchase,
                720,
                900,
                new BigDecimal("3000")
        );

        PurchaseDecisionContext decisionContext =
                new PurchaseDecisionContext(
                        true,
                        false,
                        true,
                        PurchaseMotivation.NEED
                );

        PurchaseBudgetImpact budgetImpact =
                new PurchaseBudgetImpact(
                        new BigDecimal("5000"),
                        new BigDecimal("2000"),
                        Optional.of(
                                new BigDecimal("60")
                        ),
                        PurchaseBudgetImpactStatus.WITHIN_BUDGET
                );

        FinancialProfile financialProfile =
                new FinancialProfile(
                        new BigDecimal("6000"),
                        new BigDecimal("160"),
                        new BigDecimal("20"),
                        new BigDecimal("2500"),
                        new BigDecimal("500")
                );

        PurchaseDreamImpact dreamImpact =
                new PurchaseDreamImpact(
                        "Montar meu computador",
                        new BigDecimal("10000"),
                        new BigDecimal("3000"),
                        new BigDecimal("30")
                );

        PurchaseScoreContext result = factory.create(
                analysis,
                decisionContext,
                budgetImpact,
                financialProfile,
                Optional.of(dreamImpact)
        );

        assertEquals(
                PurchaseMotivation.NEED,
                result.motivation()
        );

        assertEquals(
                PurchaseFrequency.ONCE,
                result.frequency()
        );

        assertTrue(result.planned());
        assertTrue(result.urgent());
        assertFalse(result.hasAlternative());

        assertEquals(
                720,
                result.professionalWorkMinutes()
        );

        assertEquals(
                900,
                result.realWorkMinutes()
        );

        assertEquals(
                0,
                new BigDecimal("3000").compareTo(
                        result.projectedAnnualCost()
                )
        );

        assertEquals(
                0,
                new BigDecimal("6000").compareTo(
                        result.netMonthlyIncome()
                )
        );

        assertEquals(
                0,
                new BigDecimal("60").compareTo(
                        result.budgetImpact()
                                .orElseThrow()
                )
        );

        assertEquals(
                0,
                new BigDecimal("30").compareTo(
                        result.goalImpact()
                                .orElseThrow()
                )
        );
    }

    @Test
    void shouldCreateContextWithoutOptionalImpacts() {
        Purchase purchase = new Purchase(
                "Livro",
                new BigDecimal("50"),
                PurchaseFrequency.ONCE
        );

        PurchaseAnalysis analysis = new PurchaseAnalysis(
                purchase,
                30,
                40,
                new BigDecimal("50")
        );

        PurchaseDecisionContext decisionContext =
                new PurchaseDecisionContext(
                        false,
                        true,
                        false,
                        PurchaseMotivation.DESIRE
                );

        PurchaseBudgetImpact budgetImpact =
                new PurchaseBudgetImpact(
                        BigDecimal.ZERO,
                        new BigDecimal("-50"),
                        Optional.empty(),
                        PurchaseBudgetImpactStatus
                                .NO_AVAILABLE_BUDGET
                );

        FinancialProfile financialProfile =
                new FinancialProfile(
                        new BigDecimal("3000"),
                        new BigDecimal("160"),
                        BigDecimal.ZERO
                );

        PurchaseScoreContext result = factory.create(
                analysis,
                decisionContext,
                budgetImpact,
                financialProfile,
                Optional.empty()
        );

        assertTrue(result.budgetImpact().isEmpty());
        assertTrue(result.goalImpact().isEmpty());
    }

    @Test
    void shouldRejectMissingRequiredData() {
        assertThrows(
                NullPointerException.class,
                () -> factory.create(
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );
    }
}