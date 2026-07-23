package com.kayque.compensa.alerts.rule;

import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.model.SmartAlertPriority;
import com.kayque.compensa.alerts.model.SmartAlertSnapshot;
import com.kayque.compensa.alerts.model.SmartAlertTopic;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public class FinancialGoalProgressAlertRule
        implements SmartAlertRule {

    private static final BigDecimal COMPLETED_PERCENTAGE =
            new BigDecimal("100");

    private static final BigDecimal SIGNIFICANT_DIFFERENCE =
            new BigDecimal("10");

    @Override
    public Optional<SmartAlert> evaluate(
            SmartAlertSnapshot snapshot
    ) {
        Objects.requireNonNull(
                snapshot,
                "O resumo financeiro é obrigatório."
        );

        if (!snapshot.hasFinancialGoal()) {
            return Optional.empty();
        }

        BigDecimal currentProgress =
                snapshot.goalProgressPercentage();

        BigDecimal expectedProgress =
                snapshot.expectedGoalProgressPercentage();

        if (currentProgress.compareTo(
                COMPLETED_PERCENTAGE
        ) >= 0) {
            return Optional.of(
                    new SmartAlert(
                            "financial-goal.completed",
                            SmartAlertTopic.FINANCIAL_GOAL_PROGRESS,
                            SmartAlertPriority.INFORMATIONAL,
                            "Objetivo financeiro alcançado",
                            "Parabéns! Você alcançou seu objetivo financeiro."
                    )
            );
        }

        BigDecimal difference =
                currentProgress.subtract(expectedProgress);

        if (difference.compareTo(
                SIGNIFICANT_DIFFERENCE
        ) >= 0) {
            return Optional.of(
                    new SmartAlert(
                            "financial-goal.ahead",
                            SmartAlertTopic.FINANCIAL_GOAL_PROGRESS,
                            SmartAlertPriority.INFORMATIONAL,
                            "Seu objetivo está adiantado",
                            "Seu objetivo financeiro está "
                                    + formatPercentage(difference)
                                    + " à frente do progresso planejado."
                    )
            );
        }

        if (difference.compareTo(
                SIGNIFICANT_DIFFERENCE.negate()
        ) <= 0) {
            BigDecimal delayedPercentage =
                    difference.abs();

            return Optional.of(
                    new SmartAlert(
                            "financial-goal.behind",
                            SmartAlertTopic.FINANCIAL_GOAL_PROGRESS,
                            SmartAlertPriority.ATTENTION,
                            "Seu objetivo precisa de atenção",
                            "Seu objetivo financeiro está "
                                    + formatPercentage(
                                    delayedPercentage
                            )
                                    + " atrás do progresso planejado."
                    )
            );
        }

        return Optional.empty();
    }

    private String formatPercentage(
            BigDecimal percentage
    ) {
        return percentage
                .stripTrailingZeros()
                .toPlainString()
                .replace(".", ",")
                + " pontos percentuais";
    }
}