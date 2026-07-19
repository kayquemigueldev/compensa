package com.kayque.compensa.dashboard.service;

import com.kayque.compensa.dashboard.model.DashboardHighlight;
import com.kayque.compensa.dashboard.model.DashboardHighlightType;
import com.kayque.compensa.dashboard.model.DashboardSummary;
import com.kayque.compensa.goal.model.SavingsGoalProgress;
import com.kayque.compensa.goal.model.SavingsGoalProgressStatus;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

public class DashboardHighlightService {

    private static final BigDecimal
            SEVENTY_FIVE_PERCENT =
            new BigDecimal("75");

    private static final BigDecimal
            FIFTY_PERCENT =
            new BigDecimal("50");

    private static final BigDecimal
            TWENTY_FIVE_PERCENT =
            new BigDecimal("25");

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(
                    Locale.of("pt", "BR")
            );

    public DashboardHighlight create(
            DashboardSummary summary,
            String goalName,
            SavingsGoalProgress goalProgress
    ) {
        Objects.requireNonNull(
                summary,
                "O resumo do dashboard é obrigatório."
        );

        if (hasCompletedGoal(goalProgress)) {
            return new DashboardHighlight(
                    "Objetivo alcançado!",
                    "Você completou “"
                            + normalizeGoalName(goalName)
                            + "”. Essa conquista foi construída uma contribuição de cada vez.",
                    DashboardHighlightType.SUCCESS
            );
        }

        DashboardHighlight milestoneHighlight =
                createMilestoneHighlight(
                        goalName,
                        goalProgress
                );

        if (milestoneHighlight != null) {
            return milestoneHighlight;
        }

        if (summary.waitingDecisions() > 0) {
            return new DashboardHighlight(
                    "Você possui decisões para revisar",
                    createWaitingDescription(
                            summary.waitingDecisions()
                    ),
                    DashboardHighlightType.WARNING
            );
        }

        if (summary.preservedValue().signum() > 0) {
            return new DashboardHighlight(
                    "Suas escolhas já preservaram dinheiro",
                    "Até agora, você decidiu não gastar "
                            + currencyFormat.format(
                            summary.preservedValue()
                    )
                            + ". Esse valor continua disponível para escolhas mais importantes.",
                    DashboardHighlightType.PRESERVED_VALUE
            );
        }

        if (goalProgress != null
                && goalProgress.savedAmount().signum() > 0) {
            return new DashboardHighlight(
                    "Sua conquista está avançando",
                    "Você já guardou "
                            + currencyFormat.format(
                            goalProgress.savedAmount()
                    )
                            + " para “"
                            + normalizeGoalName(goalName)
                            + "”. Continue nesse ritmo.",
                    DashboardHighlightType.GOAL
            );
        }

        return new DashboardHighlight(
                "Uma escolha de cada vez",
                "O objetivo não é parar de comprar. É entender quando uma compra realmente faz sentido para você.",
                DashboardHighlightType.DEFAULT
        );
    }

    private DashboardHighlight createMilestoneHighlight(
            String goalName,
            SavingsGoalProgress progress
    ) {
        if (progress == null) {
            return null;
        }

        BigDecimal percentage = progress.percentage();

        if (percentage.compareTo(
                SEVENTY_FIVE_PERCENT
        ) >= 0) {
            return createMilestone(
                    75,
                    goalName
            );
        }

        if (percentage.compareTo(
                FIFTY_PERCENT
        ) >= 0) {
            return createMilestone(
                    50,
                    goalName
            );
        }

        if (percentage.compareTo(
                TWENTY_FIVE_PERCENT
        ) >= 0) {
            return createMilestone(
                    25,
                    goalName
            );
        }

        return null;
    }

    private DashboardHighlight createMilestone(
            int percentage,
            String goalName
    ) {
        return new DashboardHighlight(
                percentage + "% da conquista alcançados",
                "Você já completou "
                        + percentage
                        + "% do objetivo “"
                        + normalizeGoalName(goalName)
                        + "”. Seu progresso está ficando cada vez mais visível.",
                DashboardHighlightType.GOAL
        );
    }

    private boolean hasCompletedGoal(
            SavingsGoalProgress progress
    ) {
        return progress != null
                && progress.status()
                == SavingsGoalProgressStatus.COMPLETED;
    }

    private String createWaitingDescription(long amount) {
        if (amount == 1) {
            return "Existe uma compra aguardando sua decisão. Revise com calma e registre o que escolheu.";
        }

        return "Existem "
                + amount
                + " compras aguardando sua decisão. Revise cada uma quando estiver pronto.";
    }

    private String normalizeGoalName(String goalName) {
        if (goalName == null || goalName.isBlank()) {
            return "sua próxima conquista";
        }

        return goalName.trim();
    }
}