package com.kayque.compensa.alerts.rule;

import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.model.SmartAlertPriority;
import com.kayque.compensa.alerts.model.SmartAlertSnapshot;
import com.kayque.compensa.alerts.model.SmartAlertTopic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class MonthlySavingsGoalAlertRule
        implements SmartAlertRule {

    private static final BigDecimal CLOSE_PERCENTAGE =
            new BigDecimal("20");

    private static final BigDecimal ONE_HUNDRED =
            new BigDecimal("100");

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(
                    Locale.of("pt", "BR")
            );

    @Override
    public Optional<SmartAlert> evaluate(
            SmartAlertSnapshot snapshot
    ) {
        Objects.requireNonNull(
                snapshot,
                "O resumo financeiro é obrigatório."
        );

        if (!snapshot.hasMonthlyGoal()) {
            return Optional.empty();
        }

        BigDecimal contributions =
                snapshot.monthlyGoalContributions();

        BigDecimal target =
                snapshot.monthlyGoalTarget();

        int comparison =
                contributions.compareTo(target);

        if (comparison > 0) {
            BigDecimal exceededAmount =
                    contributions.subtract(target);

            return Optional.of(
                    createAlert(
                            "monthly-goal.exceeded",
                            "Meta mensal superada",
                            "Excelente! Você superou sua meta mensal "
                                    + "de economia em "
                                    + formatMoney(exceededAmount)
                                    + "."
                    )
            );
        }

        if (comparison == 0) {
            return Optional.of(
                    createAlert(
                            "monthly-goal.reached",
                            "Meta mensal alcançada",
                            "Excelente! Você atingiu sua meta mensal de economia."
                    )
            );
        }

        BigDecimal remainingAmount =
                target.subtract(contributions);

        BigDecimal remainingPercentage =
                remainingAmount
                        .multiply(ONE_HUNDRED)
                        .divide(
                                target,
                                2,
                                RoundingMode.HALF_UP
                        );

        if (contributions.signum() > 0
                && remainingPercentage.compareTo(
                CLOSE_PERCENTAGE
        ) <= 0) {
            return Optional.of(
                    createAlert(
                            "monthly-goal.almost-reached",
                            "Você está perto da meta mensal",
                            "Faltam apenas "
                                    + formatMoney(remainingAmount)
                                    + " para alcançar sua meta deste mês."
                    )
            );
        }

        return Optional.empty();
    }

    private SmartAlert createAlert(
            String code,
            String title,
            String message
    ) {
        return new SmartAlert(
                code,
                SmartAlertTopic.MONTHLY_SAVINGS_GOAL,
                SmartAlertPriority.INFORMATIONAL,
                title,
                message
        );
    }

    private String formatMoney(BigDecimal amount) {
        return currencyFormat
                .format(amount)
                .replace('\u00A0', ' ')
                .replace('\u202F', ' ');
    }
}