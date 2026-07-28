package com.kayque.compensa.report.service;

import com.kayque.compensa.dashboard.model.DashboardSummary;
import com.kayque.compensa.dashboard.model.DashboardWeeklySummary;
import com.kayque.compensa.dashboard.repository.DashboardRepository;
import com.kayque.compensa.report.model.MonthlyFinancialOverview;
import com.kayque.compensa.report.model.MonthlySpendingTrend;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MonthlyFinancialComparisonServiceTest {

    private static final YearMonth SELECTED_MONTH =
            YearMonth.of(2026, 7);

    private static final ZoneId ZONE_ID =
            ZoneId.of("America/Sao_Paulo");

    @Test
    void shouldIdentifyLowerMonthlySpending() {
        MonthlyFinancialOverview overview =
                createComparison(
                        new BigDecimal("400"),
                        new BigDecimal("500")
                );

        assertEquals(
                MonthlySpendingTrend.LOWER,
                overview.spendingTrend()
        );

        assertEquals(
                new BigDecimal("20.0"),
                overview.spendingVariationPercentage()
        );
    }

    @Test
    void shouldIdentifyHigherMonthlySpending() {
        MonthlyFinancialOverview overview =
                createComparison(
                        new BigDecimal("600"),
                        new BigDecimal("500")
                );

        assertEquals(
                MonthlySpendingTrend.HIGHER,
                overview.spendingTrend()
        );

        assertEquals(
                new BigDecimal("20.0"),
                overview.spendingVariationPercentage()
        );
    }

    @Test
    void shouldIdentifyStableMonthlySpending() {
        MonthlyFinancialOverview overview =
                createComparison(
                        new BigDecimal("500"),
                        new BigDecimal("500")
                );

        assertEquals(
                MonthlySpendingTrend.STABLE,
                overview.spendingTrend()
        );

        assertEquals(
                new BigDecimal("0.0"),
                overview.spendingVariationPercentage()
        );
    }

    @Test
    void shouldIdentifyFirstPurchases() {
        MonthlyFinancialOverview overview =
                createComparison(
                        new BigDecimal("250"),
                        BigDecimal.ZERO
                );

        assertEquals(
                MonthlySpendingTrend.FIRST_PURCHASES,
                overview.spendingTrend()
        );

        assertEquals(
                BigDecimal.ZERO,
                overview.spendingVariationPercentage()
        );
    }

    @Test
    void shouldIdentifyMonthsWithoutPurchases() {
        MonthlyFinancialOverview overview =
                createComparison(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                );

        assertEquals(
                MonthlySpendingTrend.NO_PURCHASES,
                overview.spendingTrend()
        );

        assertEquals(
                BigDecimal.ZERO,
                overview.spendingVariationPercentage()
        );
    }

    @Test
    void shouldLoadSelectedAndPreviousMonths() {
        RecordingRepository repository =
                new RecordingRepository(
                        new BigDecimal("400"),
                        new BigDecimal("500")
                );

        MonthlyFinancialReportService service =
                new MonthlyFinancialReportService(
                        repository,
                        ZONE_ID
                );

        MonthlyFinancialOverview overview =
                service.createComparison(
                        SELECTED_MONTH
                );

        assertEquals(
                SELECTED_MONTH,
                overview.currentMonth().month()
        );

        assertEquals(
                SELECTED_MONTH.minusMonths(1),
                overview.previousMonth().month()
        );

        assertEquals(2, repository.periodStarts.size());

        assertEquals(
                Instant.parse("2026-07-01T03:00:00Z"),
                repository.periodStarts.get(0)
        );

        assertEquals(
                Instant.parse("2026-06-01T03:00:00Z"),
                repository.periodStarts.get(1)
        );
    }

    @Test
    void shouldRejectNullSelectedMonth() {
        MonthlyFinancialReportService service =
                new MonthlyFinancialReportService(
                        new RecordingRepository(
                                BigDecimal.ZERO,
                                BigDecimal.ZERO
                        ),
                        ZONE_ID
                );

        assertThrows(
                NullPointerException.class,
                () -> service.createComparison(null)
        );
    }

    private MonthlyFinancialOverview createComparison(
            BigDecimal currentPurchasedValue,
            BigDecimal previousPurchasedValue
    ) {
        MonthlyFinancialReportService service =
                new MonthlyFinancialReportService(
                        new RecordingRepository(
                                currentPurchasedValue,
                                previousPurchasedValue
                        ),
                        ZONE_ID
                );

        return service.createComparison(
                SELECTED_MONTH
        );
    }

    private static class RecordingRepository
            implements DashboardRepository {

        private final BigDecimal currentPurchasedValue;
        private final BigDecimal previousPurchasedValue;

        private final List<Instant> periodStarts =
                new ArrayList<>();

        private RecordingRepository(
                BigDecimal currentPurchasedValue,
                BigDecimal previousPurchasedValue
        ) {
            this.currentPurchasedValue =
                    currentPurchasedValue;

            this.previousPurchasedValue =
                    previousPurchasedValue;
        }

        @Override
        public DashboardSummary getSummary() {
            throw new UnsupportedOperationException();
        }

        @Override
        public DashboardWeeklySummary getWeeklySummary(
                Instant startInclusive,
                Instant endExclusive
        ) {
            periodStarts.add(startInclusive);

            boolean currentMonth =
                    startInclusive.equals(
                            Instant.parse(
                                    "2026-07-01T03:00:00Z"
                            )
                    );

            BigDecimal purchasedValue =
                    currentMonth
                            ? currentPurchasedValue
                            : previousPurchasedValue;

            return new DashboardWeeklySummary(
                    purchasedValue.signum() == 0 ? 0 : 1,
                    purchasedValue.signum() == 0 ? 0 : 1,
                    0,
                    purchasedValue,
                    BigDecimal.ZERO,
                    purchasedValue.signum() == 0 ? 0 : 60
            );
        }
    }
}