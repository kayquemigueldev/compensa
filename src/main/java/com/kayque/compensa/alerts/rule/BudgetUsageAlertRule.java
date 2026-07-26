package com.kayque.compensa.alerts.rule;

import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.model.SmartAlertPriority;
import com.kayque.compensa.alerts.model.SmartAlertSnapshot;
import com.kayque.compensa.alerts.model.SmartAlertTopic;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class BudgetUsageAlertRule
        implements SmartAlertRule {

    private static final BigDecimal EXHAUSTED_PERCENTAGE =
            new BigDecimal("100");

    private static final BigDecimal CRITICAL_PERCENTAGE =
            new BigDecimal("90");

    private static final BigDecimal ATTENTION_PERCENTAGE =
            new BigDecimal("70");

    private static final BigDecimal HEALTHY_PERCENTAGE =
            new BigDecimal("50");

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

        if (!snapshot.financialProfileConfigured()) {
            return Optional.empty();
        }

        if (snapshot.availableBudget().signum() < 0) {
            return Optional.of(
                    createAlert(
                            "budget.deficit",
                            SmartAlertPriority.CRITICAL,
                            "Seu orçamento está em déficit",
                            "As compras do mês ultrapassaram o orçamento em "
                                    + formatCurrency(
                                    snapshot.availableBudget().abs()
                            )
                                    + "."
                    )
            );
        }

        BigDecimal usagePercentage =
                snapshot.budgetUsagePercentage();

        if (usagePercentage.compareTo(
                EXHAUSTED_PERCENTAGE
        ) >= 0) {
            return Optional.of(
                    createAlert(
                            "budget.exhausted",
                            SmartAlertPriority.CRITICAL,
                            "Seu orçamento foi esgotado",
                            "Você já utilizou "
                                    + formatPercentage(
                                    usagePercentage
                            )
                                    + " do orçamento deste mês."
                    )
            );
        }

        if (usagePercentage.compareTo(
                CRITICAL_PERCENTAGE
        ) >= 0) {
            return Optional.of(
                    createAlert(
                            "budget.almost-exhausted",
                            SmartAlertPriority.CRITICAL,
                            "Seu orçamento está quase esgotado",
                            "Você já utilizou "
                                    + formatPercentage(
                                    usagePercentage
                            )
                                    + " do orçamento e possui "
                                    + formatCurrency(
                                    snapshot.availableBudget()
                            )
                                    + " disponíveis."
                    )
            );
        }

        if (usagePercentage.compareTo(
                ATTENTION_PERCENTAGE
        ) >= 0) {
            return Optional.of(
                    createAlert(
                            "budget.attention",
                            SmartAlertPriority.ATTENTION,
                            "Atenção ao orçamento deste mês",
                            "Você já utilizou "
                                    + formatPercentage(
                                    usagePercentage
                            )
                                    + " do orçamento e ainda possui "
                                    + formatCurrency(
                                    snapshot.availableBudget()
                            )
                                    + " disponíveis."
                    )
            );
        }

        if (usagePercentage.compareTo(
                HEALTHY_PERCENTAGE
        ) <= 0) {
            return Optional.of(
                    createAlert(
                            "budget.healthy",
                            SmartAlertPriority.INFORMATIONAL,
                            "Seu orçamento está sob controle",
                            "Você utilizou "
                                    + formatPercentage(
                                    usagePercentage
                            )
                                    + " do orçamento e ainda possui "
                                    + formatCurrency(
                                    snapshot.availableBudget()
                            )
                                    + " disponíveis."
                    )
            );
        }

        return Optional.empty();
    }

    private SmartAlert createAlert(
            String code,
            SmartAlertPriority priority,
            String title,
            String message
    ) {
        return new SmartAlert(
                code,
                SmartAlertTopic.BUDGET_USAGE,
                priority,
                title,
                message
        );
    }

    private String formatPercentage(
            BigDecimal percentage
    ) {
        return percentage
                .stripTrailingZeros()
                .toPlainString()
                .replace(".", ",")
                + "%";
    }

    private String formatCurrency(
            BigDecimal amount
    ) {
        return currencyFormat.format(amount);
    }
}