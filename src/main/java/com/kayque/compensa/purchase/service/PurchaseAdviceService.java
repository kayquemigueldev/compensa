package com.kayque.compensa.purchase.service;

import com.kayque.compensa.purchase.model.PurchaseAdvice;
import com.kayque.compensa.purchase.model.PurchaseAnalysis;
import com.kayque.compensa.purchase.model.PurchaseBudgetImpact;
import com.kayque.compensa.purchase.model.PurchaseDecisionContext;
import com.kayque.compensa.purchase.model.PurchaseDecisionStatus;
import com.kayque.compensa.purchase.model.PurchaseMotivation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PurchaseAdviceService {

    private static final int INITIAL_SCORE = 60;

    private static final int MAKES_SENSE_MINIMUM_SCORE = 70;
    private static final int THINK_AGAIN_MINIMUM_SCORE = 35;

    private static final long HIGH_TIME_IMPACT_MINUTES = 480;
    private static final long MEDIUM_TIME_IMPACT_MINUTES = 120;

    private static final BigDecimal
            HIGH_BUDGET_IMPACT_PERCENTAGE =
            new BigDecimal("50");

    private static final BigDecimal
            MEDIUM_BUDGET_IMPACT_PERCENTAGE =
            new BigDecimal("25");

    public PurchaseAdvice evaluate(
            PurchaseAnalysis analysis,
            PurchaseDecisionContext context
    ) {
        return evaluateInternal(
                analysis,
                context,
                null
        );
    }

    public PurchaseAdvice evaluate(
            PurchaseAnalysis analysis,
            PurchaseDecisionContext context,
            PurchaseBudgetImpact budgetImpact
    ) {
        Objects.requireNonNull(
                budgetImpact,
                "O impacto no orçamento é obrigatório."
        );

        return evaluateInternal(
                analysis,
                context,
                budgetImpact
        );
    }

    private PurchaseAdvice evaluateInternal(
            PurchaseAnalysis analysis,
            PurchaseDecisionContext context,
            PurchaseBudgetImpact budgetImpact
    ) {
        Objects.requireNonNull(
                analysis,
                "A análise financeira é obrigatória."
        );

        Objects.requireNonNull(
                context,
                "O contexto da decisão é obrigatório."
        );

        int score = INITIAL_SCORE;
        List<String> reasons = new ArrayList<>();

        if (context.planned()) {
            score += 15;
            reasons.add(
                    "A compra foi planejada anteriormente."
            );
        } else {
            score -= 10;
            reasons.add(
                    "A compra não estava planejada."
            );
        }

        score += evaluateMotivation(
                context.motivation(),
                reasons
        );

        if (context.hasAlternative()) {
            score -= 15;
            reasons.add(
                    "Existe uma alternativa disponível."
            );
        } else {
            reasons.add(
                    "Não existe uma alternativa disponível."
            );
        }

        if (context.urgent()) {
            score += 10;
            reasons.add(
                    "A compra possui utilidade imediata."
            );
        } else {
            reasons.add(
                    "A decisão pode ser adiada para reflexão."
            );
        }

        score += evaluateTimeImpact(
                analysis.realWorkMinutes(),
                reasons
        );

        if (budgetImpact != null) {
            score += evaluateBudgetImpact(
                    budgetImpact,
                    reasons
            );
        }

        int normalizedScore = normalizeScore(score);

        return new PurchaseAdvice(
                determineStatus(normalizedScore),
                normalizedScore,
                reasons
        );
    }

    private int evaluateMotivation(
            PurchaseMotivation motivation,
            List<String> reasons
    ) {
        return switch (motivation) {
            case NEED -> {
                reasons.add(
                        "A compra atende a uma necessidade."
                );
                yield 20;
            }

            case DESIRE -> {
                reasons.add(
                        "A compra está ligada a um desejo pessoal."
                );
                yield 0;
            }

            case IMPULSE -> {
                reasons.add(
                        "A decisão apresenta sinal de impulso."
                );
                yield -25;
            }
        };
    }

    private int evaluateTimeImpact(
            long realWorkMinutes,
            List<String> reasons
    ) {
        if (realWorkMinutes >= HIGH_TIME_IMPACT_MINUTES) {
            reasons.add(
                    "A compra exige pelo menos um dia de trabalho."
            );
            return -15;
        }

        if (realWorkMinutes >= MEDIUM_TIME_IMPACT_MINUTES) {
            reasons.add(
                    "A compra exige algumas horas de trabalho."
            );
            return -8;
        }

        reasons.add(
                "O impacto individual em tempo de trabalho é baixo."
        );

        return 0;
    }

    private int evaluateBudgetImpact(
            PurchaseBudgetImpact impact,
            List<String> reasons
    ) {
        return switch (impact.status()) {
            case WITHIN_BUDGET ->
                    evaluateAvailableBudgetPercentage(
                            impact,
                            reasons
                    );

            case EXCEEDS_AVAILABLE_AMOUNT -> {
                reasons.add(
                        "A compra ultrapassa o dinheiro livre deste mês."
                );
                yield -30;
            }

            case NO_AVAILABLE_BUDGET -> {
                reasons.add(
                        "Não existe dinheiro livre disponível neste mês."
                );
                yield -25;
            }

            case BUDGET_IN_DEFICIT -> {
                reasons.add(
                        "O orçamento mensal já está em déficit."
                );
                yield -30;
            }
        };
    }

    private int evaluateAvailableBudgetPercentage(
            PurchaseBudgetImpact impact,
            List<String> reasons
    ) {
        BigDecimal percentage =
                impact.budgetUsagePercentage()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "A porcentagem do orçamento deveria estar disponível."
                                )
                        );

        if (percentage.compareTo(
                HIGH_BUDGET_IMPACT_PERCENTAGE
        ) >= 0) {
            reasons.add(
                    "A compra consome pelo menos metade do dinheiro livre do mês."
            );
            return -15;
        }

        if (percentage.compareTo(
                MEDIUM_BUDGET_IMPACT_PERCENTAGE
        ) >= 0) {
            reasons.add(
                    "A compra consome uma parte relevante do dinheiro livre do mês."
            );
            return -8;
        }

        reasons.add(
                "A compra preserva a maior parte do dinheiro livre do mês."
        );

        return 0;
    }

    private PurchaseDecisionStatus determineStatus(int score) {
        if (score >= MAKES_SENSE_MINIMUM_SCORE) {
            return PurchaseDecisionStatus.MAKES_SENSE;
        }

        if (score >= THINK_AGAIN_MINIMUM_SCORE) {
            return PurchaseDecisionStatus.THINK_AGAIN;
        }

        return PurchaseDecisionStatus
                .PROBABLY_NOT_WORTH_IT;
    }

    private int normalizeScore(int score) {
        return Math.max(0, Math.min(100, score));
    }
}