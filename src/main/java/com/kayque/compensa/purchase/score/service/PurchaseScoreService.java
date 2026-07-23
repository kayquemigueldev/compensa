package com.kayque.compensa.purchase.score.service;

import com.kayque.compensa.purchase.model.PurchaseFrequency;
import com.kayque.compensa.purchase.model.PurchaseMotivation;
import com.kayque.compensa.purchase.score.model.PurchaseScore;
import com.kayque.compensa.purchase.score.model.PurchaseScoreClassification;
import com.kayque.compensa.purchase.score.model.PurchaseScoreContext;
import com.kayque.compensa.purchase.score.model.PurchaseScoreFactor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PurchaseScoreService {

    private static final int INITIAL_SCORE = 60;

    private static final BigDecimal ONE_HUNDRED =
            new BigDecimal("100");

    private static final BigDecimal MONTHS_PER_YEAR =
            new BigDecimal("12");

    private final PurchaseScoreWeights weights;

    private final PurchaseScoreJustificationService
            justificationService;

    public PurchaseScoreService() {
        this(
                PurchaseScoreWeights.defaultWeights(),
                new PurchaseScoreJustificationService()
        );
    }

    public PurchaseScoreService(
            PurchaseScoreWeights weights,
            PurchaseScoreJustificationService
                    justificationService
    ) {
        this.weights = Objects.requireNonNull(
                weights,
                "Os pesos do Score Compensa? são obrigatórios."
        );

        this.justificationService =
                Objects.requireNonNull(
                        justificationService,
                        "O serviço de justificativa é obrigatório."
                );
    }

    public PurchaseScore calculate(
            PurchaseScoreContext context
    ) {
        Objects.requireNonNull(
                context,
                "O contexto do Score Compensa? é obrigatório."
        );

        List<PurchaseScoreFactor> factors =
                new ArrayList<>();

        evaluatePlanning(context, factors);
        evaluateMotivation(context, factors);
        evaluateUrgency(context, factors);
        evaluateAlternative(context, factors);
        evaluateBudgetImpact(context, factors);
        evaluateWorkTime(context, factors);
        evaluateGoalImpact(context, factors);
        evaluateFrequency(context, factors);
        evaluateAnnualProjection(context, factors);

        int calculatedScore = INITIAL_SCORE
                + factors.stream()
                .mapToInt(PurchaseScoreFactor::points)
                .sum();

        int normalizedScore = Math.max(
                0,
                Math.min(100, calculatedScore)
        );

        PurchaseScoreClassification classification =
                PurchaseScoreClassification.fromScore(
                        normalizedScore
                );

        List<PurchaseScoreFactor> positiveFactors =
                factors.stream()
                        .filter(
                                PurchaseScoreFactor::isPositive
                        )
                        .toList();

        List<PurchaseScoreFactor> negativeFactors =
                factors.stream()
                        .filter(
                                PurchaseScoreFactor::isNegative
                        )
                        .toList();

        return new PurchaseScore(
                normalizedScore,
                classification,
                positiveFactors,
                negativeFactors,
                justificationService.create(classification)
        );
    }

    private void evaluatePlanning(
            PurchaseScoreContext context,
            List<PurchaseScoreFactor> factors
    ) {
        if (context.planned()) {
            addFactor(
                    factors,
                    PurchaseScoreCriterion.PLANNED_PURCHASE,
                    "A compra foi planejada."
            );

            return;
        }

        addFactor(
                factors,
                PurchaseScoreCriterion.UNPLANNED_PURCHASE,
                "A compra não estava planejada."
        );
    }

    private void evaluateMotivation(
            PurchaseScoreContext context,
            List<PurchaseScoreFactor> factors
    ) {
        if (
                context.motivation()
                        == PurchaseMotivation.NEED
        ) {
            addFactor(
                    factors,
                    PurchaseScoreCriterion.NECESSITY_MOTIVATION,
                    "A compra atende a uma necessidade."
            );

            return;
        }

        if (
                context.motivation()
                        == PurchaseMotivation.IMPULSE
        ) {
            addFactor(
                    factors,
                    PurchaseScoreCriterion.IMPULSE_MOTIVATION,
                    "A decisão apresenta sinal de impulso."
            );
        }
    }

    private void evaluateUrgency(
            PurchaseScoreContext context,
            List<PurchaseScoreFactor> factors
    ) {
        if (!context.urgent()) {
            return;
        }

        addFactor(
                factors,
                PurchaseScoreCriterion.URGENT_PURCHASE,
                "A compra possui utilidade imediata."
        );
    }

    private void evaluateAlternative(
            PurchaseScoreContext context,
            List<PurchaseScoreFactor> factors
    ) {
        if (context.hasAlternative()) {
            addFactor(
                    factors,
                    PurchaseScoreCriterion.HAS_ALTERNATIVE,
                    "Existe uma alternativa disponível."
            );

            return;
        }

        addFactor(
                factors,
                PurchaseScoreCriterion.HAS_NO_ALTERNATIVE,
                "Não existe uma alternativa disponível."
        );
    }

    private void evaluateBudgetImpact(
            PurchaseScoreContext context,
            List<PurchaseScoreFactor> factors
    ) {
        context.budgetImpact().ifPresent(
                percentage -> {
                    if (isAtMost(percentage, "10")) {
                        addFactor(
                                factors,
                                PurchaseScoreCriterion.LOW_BUDGET_IMPACT,
                                "A compra possui baixo impacto no orçamento mensal."
                        );

                    } else if (isAtMost(percentage, "25")) {
                        addFactor(
                                factors,
                                PurchaseScoreCriterion.MODERATE_BUDGET_IMPACT,
                                "A compra utiliza uma parte moderada do orçamento mensal."
                        );

                    } else if (isAtMost(percentage, "50")) {
                        addFactor(
                                factors,
                                PurchaseScoreCriterion.HIGH_BUDGET_IMPACT,
                                "A compra consome uma parte relevante do orçamento mensal."
                        );

                    } else {
                        addFactor(
                                factors,
                                PurchaseScoreCriterion.CRITICAL_BUDGET_IMPACT,
                                "A compra compromete mais da metade do orçamento mensal."
                        );
                    }
                }
        );
    }

    private void evaluateWorkTime(
            PurchaseScoreContext context,
            List<PurchaseScoreFactor> factors
    ) {
        long realMinutes = context.realWorkMinutes();

        if (realMinutes < 60) {
            addFactor(
                    factors,
                    PurchaseScoreCriterion.LOW_WORK_TIME,
                    "A compra exige menos de uma hora de trabalho."
            );

        } else if (realMinutes >= 480) {
            addFactor(
                    factors,
                    PurchaseScoreCriterion.CRITICAL_WORK_TIME,
                    "A compra exige pelo menos um dia completo de trabalho."
            );

        } else if (realMinutes >= 240) {
            addFactor(
                    factors,
                    PurchaseScoreCriterion.HIGH_WORK_TIME,
                    "A compra exige várias horas de trabalho."
            );
        }

        long additionalMinutes =
                context.realWorkMinutes()
                        - context.professionalWorkMinutes();

        if (additionalMinutes >= 120) {
            addFactor(
                    factors,
                    PurchaseScoreCriterion.HIGH_ADDITIONAL_TIME,
                    "O tempo adicional comprometido aumenta o custo real da compra."
            );
        }
    }

    private void evaluateGoalImpact(
            PurchaseScoreContext context,
            List<PurchaseScoreFactor> factors
    ) {
        context.goalImpact().ifPresent(
                percentage -> {
                    if (isAtMost(percentage, "2")) {
                        addFactor(
                                factors,
                                PurchaseScoreCriterion.LOW_GOAL_IMPACT,
                                "A compra possui baixo impacto no objetivo financeiro."
                        );

                    } else if (
                            percentage.compareTo(
                                    new BigDecimal("25")
                            ) >= 0
                    ) {
                        addFactor(
                                factors,
                                PurchaseScoreCriterion.CRITICAL_GOAL_IMPACT,
                                "A compra representa pelo menos um quarto do objetivo financeiro."
                        );

                    } else if (
                            percentage.compareTo(
                                    new BigDecimal("10")
                            ) >= 0
                    ) {
                        addFactor(
                                factors,
                                PurchaseScoreCriterion.HIGH_GOAL_IMPACT,
                                "A compra afasta de forma relevante o objetivo financeiro."
                        );
                    }
                }
        );
    }

    private void evaluateFrequency(
            PurchaseScoreContext context,
            List<PurchaseScoreFactor> factors
    ) {
        if (
                context.frequency()
                        == PurchaseFrequency.MONTHLY
        ) {
            addFactor(
                    factors,
                    PurchaseScoreCriterion.MONTHLY_FREQUENCY,
                    "A compra cria um custo mensal recorrente."
            );

        } else if (
                context.frequency()
                        == PurchaseFrequency.WEEKLY
        ) {
            addFactor(
                    factors,
                    PurchaseScoreCriterion.WEEKLY_FREQUENCY,
                    "A frequência semanal aumenta o impacto acumulado."
            );

        } else if (
                context.frequency()
                        == PurchaseFrequency.DAILY
        ) {
            addFactor(
                    factors,
                    PurchaseScoreCriterion.DAILY_FREQUENCY,
                    "A frequência diária produz alto impacto acumulado."
            );
        }
    }

    private void evaluateAnnualProjection(
            PurchaseScoreContext context,
            List<PurchaseScoreFactor> factors
    ) {
        BigDecimal annualIncome =
                context.netMonthlyIncome()
                        .multiply(MONTHS_PER_YEAR);

        BigDecimal annualPercentage =
                context.projectedAnnualCost()
                        .multiply(ONE_HUNDRED)
                        .divide(
                                annualIncome,
                                2,
                                RoundingMode.HALF_UP
                        );

        if (
                annualPercentage.compareTo(
                        new BigDecimal("25")
                ) >= 0
        ) {
            addFactor(
                    factors,
                    PurchaseScoreCriterion.CRITICAL_ANNUAL_PROJECTION,
                    "A projeção anual compromete uma parcela crítica da renda."
            );

        } else if (
                annualPercentage.compareTo(
                        new BigDecimal("10")
                ) >= 0
        ) {
            addFactor(
                    factors,
                    PurchaseScoreCriterion.HIGH_ANNUAL_PROJECTION,
                    "A projeção anual possui impacto relevante sobre a renda."
            );
        }
    }

    private boolean isAtMost(
            BigDecimal value,
            String maximum
    ) {
        return value.compareTo(
                new BigDecimal(maximum)
        ) <= 0;
    }

    private void addFactor(
            List<PurchaseScoreFactor> factors,
            PurchaseScoreCriterion criterion,
            String description
    ) {
        factors.add(
                new PurchaseScoreFactor(
                        description,
                        weights.pointsFor(criterion)
                )
        );
    }
}