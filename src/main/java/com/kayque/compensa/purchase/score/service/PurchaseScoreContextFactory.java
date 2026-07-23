package com.kayque.compensa.purchase.score.service;

import com.kayque.compensa.profile.model.FinancialProfile;
import com.kayque.compensa.purchase.model.PurchaseAnalysis;
import com.kayque.compensa.purchase.model.PurchaseBudgetImpact;
import com.kayque.compensa.purchase.model.PurchaseDecisionContext;
import com.kayque.compensa.purchase.score.model.PurchaseScoreContext;
import com.kayque.compensa.userprofile.model.PurchaseDreamImpact;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public class PurchaseScoreContextFactory {

    public PurchaseScoreContext create(
            PurchaseAnalysis analysis,
            PurchaseDecisionContext decisionContext,
            PurchaseBudgetImpact budgetImpact,
            FinancialProfile financialProfile,
            Optional<PurchaseDreamImpact> dreamImpact
    ) {
        Objects.requireNonNull(
                analysis,
                "A análise da compra é obrigatória."
        );

        Objects.requireNonNull(
                decisionContext,
                "O contexto da decisão é obrigatório."
        );

        Objects.requireNonNull(
                budgetImpact,
                "O impacto no orçamento é obrigatório."
        );

        Objects.requireNonNull(
                financialProfile,
                "O perfil financeiro é obrigatório."
        );

        Objects.requireNonNull(
                dreamImpact,
                "O impacto no objetivo é obrigatório."
        );

        BigDecimal budgetPercentage =
                budgetImpact.budgetUsagePercentage()
                        .orElse(null);

        BigDecimal goalPercentage =
                dreamImpact
                        .map(PurchaseDreamImpact::targetPercentage)
                        .orElse(null);

        return new PurchaseScoreContext(
                decisionContext.motivation(),
                analysis.purchase().frequency(),
                decisionContext.planned(),
                decisionContext.urgent(),
                decisionContext.hasAlternative(),
                analysis.professionalWorkMinutes(),
                analysis.realWorkMinutes(),
                analysis.projectedYearlyCost(),
                financialProfile.netMonthlyIncome(),
                budgetPercentage,
                goalPercentage
        );
    }
}