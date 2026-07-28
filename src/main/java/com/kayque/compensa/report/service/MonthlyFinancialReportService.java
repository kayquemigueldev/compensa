package com.kayque.compensa.report.service;

import com.kayque.compensa.dashboard.model.DashboardWeeklySummary;
import com.kayque.compensa.dashboard.repository.DashboardRepository;
import com.kayque.compensa.report.model.MonthlyFinancialReport;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Objects;

public class MonthlyFinancialReportService {

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
}
