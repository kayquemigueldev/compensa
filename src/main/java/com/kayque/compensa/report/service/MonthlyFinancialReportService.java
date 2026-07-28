package com.kayque.compensa.report.service;

import com.kayque.compensa.dashboard.model.DashboardWeeklySummary;
import com.kayque.compensa.dashboard.repository.DashboardRepository;
import com.kayque.compensa.report.model.MonthlyFinancialOverview;
import com.kayque.compensa.report.model.MonthlyFinancialReport;
import com.kayque.compensa.report.model.MonthlySpendingTrend;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Objects;

public class MonthlyFinancialReportService {

    private static final BigDecimal ONE_HUNDRED =
            new BigDecimal("100");

    private final DashboardRepository repository;
    private final ZoneId zoneId;

    public MonthlyFinancialReportService(
            DashboardRepository repository
    ) {
        this(
                repository,
                ZoneId.systemDefault()
        );
    }

    public MonthlyFinancialReportService(
            DashboardRepository repository,
            ZoneId zoneId
    ) {
        this.repository = Objects.requireNonNull(
                repository,
                "O repositório é obrigatório."
        );

        this.zoneId = Objects.requireNonNull(
                zoneId,
                "O fuso horário é obrigatório."
        );
    }

    public MonthlyFinancialReport create(
            YearMonth month
    ) {
        Objects.requireNonNull(
                month,
                "O mês do relatório é obrigatório."
        );

        Instant startInclusive =
                month.atDay(1)
                        .atStartOfDay(zoneId)
                        .toInstant();

        Instant endExclusive =
                month.plusMonths(1)
                        .atDay(1)
                        .atStartOfDay(zoneId)
                        .toInstant();

        DashboardWeeklySummary summary =
                repository.getWeeklySummary(
                        startInclusive,
                        endExclusive
                );

        long waitingDecisions =
                summary.totalDecisions()
                        - summary.purchasedDecisions()
                        - summary.declinedDecisions();

        return new MonthlyFinancialReport(
                month,
                summary.totalDecisions(),
                summary.purchasedDecisions(),
                summary.declinedDecisions(),
                waitingDecisions,
                summary.purchasedValue(),
                summary.preservedValue(),
                summary.totalRealWorkMinutes()
        );
    }

    public MonthlyFinancialOverview createComparison(
            YearMonth selectedMonth
    ) {
        Objects.requireNonNull(
                selectedMonth,
                "O mês selecionado é obrigatório."
        );

        MonthlyFinancialReport currentMonth =
                create(selectedMonth);

        MonthlyFinancialReport previousMonth =
                create(
                        selectedMonth.minusMonths(1)
                );

        MonthlySpendingTrend spendingTrend =
                determineSpendingTrend(
                        currentMonth.purchasedValue(),
                        previousMonth.purchasedValue()
                );

        BigDecimal variationPercentage =
                calculateSpendingVariationPercentage(
                        currentMonth.purchasedValue(),
                        previousMonth.purchasedValue()
                );

        return new MonthlyFinancialOverview(
                selectedMonth,
                currentMonth,
                previousMonth,
                spendingTrend,
                variationPercentage
        );
    }

    private MonthlySpendingTrend determineSpendingTrend(
            BigDecimal currentPurchasedValue,
            BigDecimal previousPurchasedValue
    ) {
        boolean currentMonthHasNoPurchases =
                currentPurchasedValue.signum() == 0;

        boolean previousMonthHasNoPurchases =
                previousPurchasedValue.signum() == 0;

        if (currentMonthHasNoPurchases
                && previousMonthHasNoPurchases) {
            return MonthlySpendingTrend.NO_PURCHASES;
        }

        if (!currentMonthHasNoPurchases
                && previousMonthHasNoPurchases) {
            return MonthlySpendingTrend.FIRST_PURCHASES;
        }

        int comparison =
                currentPurchasedValue.compareTo(
                        previousPurchasedValue
                );

        if (comparison < 0) {
            return MonthlySpendingTrend.LOWER;
        }

        if (comparison > 0) {
            return MonthlySpendingTrend.HIGHER;
        }

        return MonthlySpendingTrend.STABLE;
    }

    private BigDecimal calculateSpendingVariationPercentage(
            BigDecimal currentPurchasedValue,
            BigDecimal previousPurchasedValue
    ) {
        if (previousPurchasedValue.signum() == 0) {
            return BigDecimal.ZERO;
        }

        return currentPurchasedValue
                .subtract(previousPurchasedValue)
                .abs()
                .multiply(ONE_HUNDRED)
                .divide(
                        previousPurchasedValue,
                        1,
                        RoundingMode.HALF_UP
                );
    }
}