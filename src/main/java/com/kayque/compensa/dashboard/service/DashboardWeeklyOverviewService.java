package com.kayque.compensa.dashboard.service;

import com.kayque.compensa.dashboard.model.DashboardWeeklyOverview;
import com.kayque.compensa.dashboard.model.DashboardWeeklySpendingTrend;
import com.kayque.compensa.dashboard.model.DashboardWeeklySummary;
import com.kayque.compensa.dashboard.repository.DashboardRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

public class DashboardWeeklyOverviewService {

    private static final BigDecimal ONE_HUNDRED =
            new BigDecimal("100");

    private final DashboardRepository repository;
    private final Clock clock;
    private final ZoneId zoneId;

    public DashboardWeeklyOverviewService(
            DashboardRepository repository
    ) {
        this(
                repository,
                Clock.systemDefaultZone(),
                ZoneId.systemDefault()
        );
    }

    public DashboardWeeklyOverviewService(
            DashboardRepository repository,
            Clock clock,
            ZoneId zoneId
    ) {
        this.repository = Objects.requireNonNull(
                repository,
                "O repositório do dashboard é obrigatório."
        );

        this.clock = Objects.requireNonNull(
                clock,
                "O relógio é obrigatório."
        );

        this.zoneId = Objects.requireNonNull(
                zoneId,
                "O fuso horário é obrigatório."
        );
    }

    public DashboardWeeklyOverview create() {
        LocalDate today = LocalDate.now(clock);

        LocalDate currentWeekStart =
                today.with(
                        TemporalAdjusters.previousOrSame(
                                DayOfWeek.MONDAY
                        )
                );

        LocalDate currentWeekEnd =
                currentWeekStart.plusDays(6);

        ZonedDateTime currentStart =
                currentWeekStart.atStartOfDay(zoneId);

        ZonedDateTime nextWeekStart =
                currentStart.plusWeeks(1);

        ZonedDateTime previousWeekStart =
                currentStart.minusWeeks(1);

        DashboardWeeklySummary currentWeek =
                repository.getWeeklySummary(
                        currentStart.toInstant(),
                        nextWeekStart.toInstant()
                );

        DashboardWeeklySummary previousWeek =
                repository.getWeeklySummary(
                        previousWeekStart.toInstant(),
                        currentStart.toInstant()
                );

        DashboardWeeklySpendingTrend trend =
                determineTrend(
                        currentWeek.purchasedValue(),
                        previousWeek.purchasedValue()
                );

        BigDecimal variationPercentage =
                calculateVariationPercentage(
                        currentWeek.purchasedValue(),
                        previousWeek.purchasedValue()
                );

        return new DashboardWeeklyOverview(
                currentWeekStart,
                currentWeekEnd,
                currentWeek,
                previousWeek,
                trend,
                variationPercentage
        );
    }

    private DashboardWeeklySpendingTrend determineTrend(
            BigDecimal currentValue,
            BigDecimal previousValue
    ) {
        boolean currentIsZero =
                currentValue.signum() == 0;

        boolean previousIsZero =
                previousValue.signum() == 0;

        if (currentIsZero && previousIsZero) {
            return DashboardWeeklySpendingTrend
                    .NO_PURCHASES;
        }

        if (previousIsZero) {
            return DashboardWeeklySpendingTrend
                    .FIRST_PURCHASES;
        }

        int comparison =
                currentValue.compareTo(previousValue);

        if (comparison < 0) {
            return DashboardWeeklySpendingTrend.LOWER;
        }

        if (comparison > 0) {
            return DashboardWeeklySpendingTrend.HIGHER;
        }

        return DashboardWeeklySpendingTrend.STABLE;
    }

    private BigDecimal calculateVariationPercentage(
            BigDecimal currentValue,
            BigDecimal previousValue
    ) {
        if (previousValue.signum() == 0) {
            return BigDecimal.ZERO;
        }

        return currentValue
                .subtract(previousValue)
                .abs()
                .multiply(ONE_HUNDRED)
                .divide(
                        previousValue,
                        1,
                        RoundingMode.HALF_UP
                );
    }
}