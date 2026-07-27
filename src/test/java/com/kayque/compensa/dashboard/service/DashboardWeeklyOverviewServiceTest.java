package com.kayque.compensa.dashboard.service;

import com.kayque.compensa.dashboard.model.DashboardSummary;
import com.kayque.compensa.dashboard.model.DashboardWeeklyOverview;
import com.kayque.compensa.dashboard.model.DashboardWeeklySpendingTrend;
import com.kayque.compensa.dashboard.model.DashboardWeeklySummary;
import com.kayque.compensa.dashboard.repository.DashboardRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardWeeklyOverviewServiceTest {

    private static final ZoneId USER_ZONE =
            ZoneId.of("America/Sao_Paulo");

    private static final Clock FIXED_CLOCK =
            Clock.fixed(
                    Instant.parse(
                            "2026-07-29T15:00:00Z"
                    ),
                    USER_ZONE
            );

    @Test
    void shouldCalculateCurrentAndPreviousWeekPeriods() {
        FakeDashboardRepository repository =
                new FakeDashboardRepository(
                        weeklySummary("100.00"),
                        weeklySummary("200.00")
                );

        DashboardWeeklyOverviewService service =
                new DashboardWeeklyOverviewService(
                        repository,
                        FIXED_CLOCK,
                        USER_ZONE
                );

        DashboardWeeklyOverview overview =
                service.create();

        assertEquals(
                LocalDate.of(2026, 7, 27),
                overview.weekStart()
        );

        assertEquals(
                LocalDate.of(2026, 8, 2),
                overview.weekEnd()
        );

        assertEquals(
                Instant.parse(
                        "2026-07-27T03:00:00Z"
                ),
                repository.periods.get(0).start()
        );

        assertEquals(
                Instant.parse(
                        "2026-08-03T03:00:00Z"
                ),
                repository.periods.get(0).end()
        );

        assertEquals(
                Instant.parse(
                        "2026-07-20T03:00:00Z"
                ),
                repository.periods.get(1).start()
        );

        assertEquals(
                Instant.parse(
                        "2026-07-27T03:00:00Z"
                ),
                repository.periods.get(1).end()
        );
    }

    @Test
    void shouldIdentifyLowerWeeklySpending() {
        FakeDashboardRepository repository =
                new FakeDashboardRepository(
                        weeklySummary("100.00"),
                        weeklySummary("200.00")
                );

        DashboardWeeklyOverview overview =
                createService(repository).create();

        assertEquals(
                DashboardWeeklySpendingTrend.LOWER,
                overview.spendingTrend()
        );

        assertEquals(
                new BigDecimal("50.0"),
                overview.spendingVariationPercentage()
        );
    }

    @Test
    void shouldIdentifyHigherWeeklySpending() {
        FakeDashboardRepository repository =
                new FakeDashboardRepository(
                        weeklySummary("300.00"),
                        weeklySummary("200.00")
                );

        DashboardWeeklyOverview overview =
                createService(repository).create();

        assertEquals(
                DashboardWeeklySpendingTrend.HIGHER,
                overview.spendingTrend()
        );

        assertEquals(
                new BigDecimal("50.0"),
                overview.spendingVariationPercentage()
        );
    }

    @Test
    void shouldIdentifyStableWeeklySpending() {
        FakeDashboardRepository repository =
                new FakeDashboardRepository(
                        weeklySummary("200.00"),
                        weeklySummary("200.00")
                );

        DashboardWeeklyOverview overview =
                createService(repository).create();

        assertEquals(
                DashboardWeeklySpendingTrend.STABLE,
                overview.spendingTrend()
        );

        assertEquals(
                new BigDecimal("0.0"),
                overview.spendingVariationPercentage()
        );
    }

    @Test
    void shouldIdentifyFirstPurchasesWhenPreviousWeekIsEmpty() {
        FakeDashboardRepository repository =
                new FakeDashboardRepository(
                        weeklySummary("100.00"),
                        weeklySummary("0")
                );

        DashboardWeeklyOverview overview =
                createService(repository).create();

        assertEquals(
                DashboardWeeklySpendingTrend.FIRST_PURCHASES,
                overview.spendingTrend()
        );

        assertEquals(
                BigDecimal.ZERO,
                overview.spendingVariationPercentage()
        );
    }

    @Test
    void shouldIdentifyWeeksWithoutPurchases() {
        FakeDashboardRepository repository =
                new FakeDashboardRepository(
                        weeklySummary("0"),
                        weeklySummary("0")
                );

        DashboardWeeklyOverview overview =
                createService(repository).create();

        assertEquals(
                DashboardWeeklySpendingTrend.NO_PURCHASES,
                overview.spendingTrend()
        );
    }

    private DashboardWeeklyOverviewService createService(
            DashboardRepository repository
    ) {
        return new DashboardWeeklyOverviewService(
                repository,
                FIXED_CLOCK,
                USER_ZONE
        );
    }

    private DashboardWeeklySummary weeklySummary(
            String purchasedValue
    ) {
        return new DashboardWeeklySummary(
                1,
                1,
                0,
                new BigDecimal(purchasedValue),
                BigDecimal.ZERO,
                60
        );
    }

    private record Period(
            Instant start,
            Instant end
    ) {
    }

    private static class FakeDashboardRepository
            implements DashboardRepository {

        private final List<DashboardWeeklySummary>
                summaries;

        private final List<Period> periods =
                new ArrayList<>();

        private int currentIndex;

        private FakeDashboardRepository(
                DashboardWeeklySummary... summaries
        ) {
            this.summaries = List.of(summaries);
        }

        @Override
        public DashboardSummary getSummary() {
            return new DashboardSummary(
                    0,
                    0,
                    0,
                    0,
                    BigDecimal.ZERO,
                    0
            );
        }

        @Override
        public DashboardWeeklySummary getWeeklySummary(
                Instant startInclusive,
                Instant endExclusive
        ) {
            periods.add(
                    new Period(
                            startInclusive,
                            endExclusive
                    )
            );

            return summaries.get(currentIndex++);
        }
    }
}