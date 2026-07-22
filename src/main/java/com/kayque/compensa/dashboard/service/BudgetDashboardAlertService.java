package com.kayque.compensa.dashboard.service;

import com.kayque.compensa.dashboard.model.DashboardAlert;
import com.kayque.compensa.dashboard.model.DashboardAlertLevel;
import com.kayque.compensa.profile.model.MonthlyBudgetStatus;
import com.kayque.compensa.profile.model.MonthlyBudgetUsage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;

public class BudgetDashboardAlertService {

    private static final BigDecimal
            ATTENTION_PERCENTAGE =
            new BigDecimal("70");

    private static final BigDecimal
            CRITICAL_PERCENTAGE =
            new BigDecimal("90");

    private static final BigDecimal
            LIMIT_PERCENTAGE =
            new BigDecimal("100");

    public Optional<DashboardAlert> create(
            MonthlyBudgetUsage usage
    ) {
        Objects.requireNonNull(
                usage,
                "O uso do orçamento é obrigatório."
        );

        if (usage.plannedAvailableAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.of(
                    new DashboardAlert(
                            "Sem dinheiro livre planejado",
                            "Seu orçamento não possui margem disponível para novas escolhas neste mês.",
                            DashboardAlertLevel.ATTENTION
                    )
            );
        }

        BigDecimal percentage =
                calculatePercentage(usage);

        if (usage.status() == MonthlyBudgetStatus.DEFICIT
                || percentage.compareTo(
                LIMIT_PERCENTAGE
        ) > 0) {
            return Optional.of(
                    new DashboardAlert(
                            "Orçamento mensal ultrapassado",
                            "As compras deste mês ultrapassaram o dinheiro livre planejado. Revise as próximas escolhas com cuidado.",
                            DashboardAlertLevel.ATTENTION
                    )
            );
        }

        if (usage.status() == MonthlyBudgetStatus.BALANCED
                || percentage.compareTo(
                LIMIT_PERCENTAGE
        ) == 0) {
            return Optional.of(
                    new DashboardAlert(
                            "Orçamento mensal no limite",
                            "Todo o dinheiro livre planejado para este mês já foi utilizado.",
                            DashboardAlertLevel.ATTENTION
                    )
            );
        }

        if (percentage.compareTo(
                CRITICAL_PERCENTAGE
        ) >= 0) {
            return Optional.of(
                    new DashboardAlert(
                            "Pouca margem disponível",
                            "Você já utilizou "
                                    + formatPercentage(percentage)
                                    + " do dinheiro livre planejado para este mês.",
                            DashboardAlertLevel.ATTENTION
                    )
            );
        }

        if (percentage.compareTo(
                ATTENTION_PERCENTAGE
        ) >= 0) {
            return Optional.of(
                    new DashboardAlert(
                            "Orçamento entrando em atenção",
                            "Você já utilizou "
                                    + formatPercentage(percentage)
                                    + " do dinheiro livre planejado. Ainda existe margem, mas vale acompanhar as próximas escolhas.",
                            DashboardAlertLevel.INFORMATION
                    )
            );
        }

        return Optional.empty();
    }

    private BigDecimal calculatePercentage(
            MonthlyBudgetUsage usage
    ) {
        return usage.purchasedAmount()
                .multiply(new BigDecimal("100"))
                .divide(
                        usage.plannedAvailableAmount(),
                        2,
                        RoundingMode.HALF_UP
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
}