package com.kayque.compensa.insights.service;

import com.kayque.compensa.insights.model.InsightReport;
import com.kayque.compensa.insights.model.InsightsSummary;

import java.util.Objects;

public class InsightsService {

    private static final long MINIMUM_DECISIONS_FOR_PATTERN = 5;
    private static final long MINIMUM_EVALUATIONS_FOR_SATISFACTION_PATTERN = 3;

    public InsightReport generateReport(
            InsightsSummary summary
    ) {
        Objects.requireNonNull(
                summary,
                "O resumo dos insights é obrigatório."
        );

        if (summary.totalDecisions() == 0) {
            return createEmptyReport();
        }

        int purchaseRate = calculateRate(
                summary.purchasedDecisions(),
                summary.totalDecisions()
        );

        int declineRate = calculateRate(
                summary.declinedDecisions(),
                summary.totalDecisions()
        );

        int waitingRate = calculateRate(
                summary.waitingDecisions(),
                summary.totalDecisions()
        );

        long averageMinutes = Math.round(
                (double) summary.totalRealWorkMinutes()
                        / summary.totalDecisions()
        );

        int satisfactionRate = calculateEvaluationRate(
                summary.worthItPurchases(),
                summary.evaluatedPurchases()
        );

        int regretRate = calculateEvaluationRate(
                summary.regrettedPurchases(),
                summary.evaluatedPurchases()
        );

        InsightMessage message =
                determineInsightMessage(
                        summary,
                        declineRate,
                        satisfactionRate,
                        regretRate
                );

        return new InsightReport(
                purchaseRate,
                declineRate,
                waitingRate,
                averageMinutes,
                satisfactionRate,
                regretRate,
                summary.evaluatedPurchases(),
                message.headline(),
                message.description()
        );
    }

    private InsightMessage determineInsightMessage(
            InsightsSummary summary,
            int declineRate,
            int satisfactionRate,
            int regretRate
    ) {
        if (summary.totalDecisions()
                < MINIMUM_DECISIONS_FOR_PATTERN) {
            return new InsightMessage(
                    "Continue registrando suas decisões",
                    "Ainda existem poucos dados para identificar um padrão confiável. Cada nova análise ajuda o Compensa? a entender melhor suas escolhas."
            );
        }

        if (hasEnoughSatisfactionEvaluations(summary)) {
            if (regretRate >= 50) {
                return new InsightMessage(
                        "Algumas compras estão gerando arrependimento",
                        "Metade ou mais das compras avaliadas não valeu a pena para você. Antes da próxima decisão, observe se existem pressa, impulso ou alternativas disponíveis."
                );
            }

            if (satisfactionRate >= 70) {
                return new InsightMessage(
                        "Suas compras avaliadas costumam valer a pena",
                        "A maioria das compras avaliadas trouxe uma experiência positiva. Continue registrando o contexto para descobrir quais tipos de escolha funcionam melhor para você."
                );
            }

            if (hasFrequentPartialSatisfaction(summary)) {
                return new InsightMessage(
                        "Suas compras estão deixando dúvidas",
                        "Muitas compras foram avaliadas como mais ou menos. Isso pode indicar que elas não foram ruins, mas também não entregaram todo o valor esperado."
                );
            }
        }

        if (hasFrequentPurchasesAgainstAdvice(summary)) {
            return new InsightMessage(
                    "Observe as compras feitas após alertas",
                    "Você costuma comprar mesmo quando existem pontos importantes para reflexão. Isso não torna as compras erradas, mas vale acompanhar a satisfação depois delas."
            );
        }

        if (declineRate >= 40) {
            return new InsightMessage(
                    "Você está criando espaço antes de gastar",
                    "Uma parte relevante das compras analisadas não foi realizada. Seu histórico indica que parar para refletir está influenciando suas decisões."
            );
        }

        return new InsightMessage(
                "Suas decisões estão ganhando consistência",
                "Você já possui dados suficientes para começar a reconhecer padrões. Continue registrando o resultado das compras para tornar os insights mais úteis."
        );
    }

    private boolean hasEnoughSatisfactionEvaluations(
            InsightsSummary summary
    ) {
        return summary.evaluatedPurchases()
                >= MINIMUM_EVALUATIONS_FOR_SATISFACTION_PATTERN;
    }

    private boolean hasFrequentPartialSatisfaction(
            InsightsSummary summary
    ) {
        return summary.partiallyWorthItPurchases() * 2
                >= summary.evaluatedPurchases();
    }

    private boolean hasFrequentPurchasesAgainstAdvice(
            InsightsSummary summary
    ) {
        if (summary.purchasedDecisions() == 0) {
            return false;
        }

        return summary.purchasesAgainstAdvice() * 2
                >= summary.purchasedDecisions();
    }

    private int calculateRate(
            long amount,
            long total
    ) {
        return (int) Math.round(
                amount * 100.0 / total
        );
    }

    private int calculateEvaluationRate(
            long amount,
            long evaluatedPurchases
    ) {
        if (evaluatedPurchases == 0) {
            return 0;
        }

        return calculateRate(
                amount,
                evaluatedPurchases
        );
    }

    private InsightReport createEmptyReport() {
        return new InsightReport(
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                "Comece pela sua primeira decisão",
                "Analise uma compra para que o Compensa? comece a construir insights sobre suas escolhas."
        );
    }

    private record InsightMessage(
            String headline,
            String description
    ) {
    }
}