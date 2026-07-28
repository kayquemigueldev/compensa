package com.kayque.compensa.report.service;

import com.kayque.compensa.dashboard.model.DashboardSummary;
import com.kayque.compensa.dashboard.model.DashboardWeeklySummary;
import com.kayque.compensa.dashboard.repository.DashboardRepository;
import com.kayque.compensa.report.model.MonthlyFinancialReport;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MonthlyFinancialReportServiceTest {

    @Test
    void shouldCreateReportForSelectedMonth() {
        RecordingRepository repository =
                new RecordingRepository();

        MonthlyFinancialReportService service =
                new MonthlyFinancialReportService(
                        repository,
                        ZoneId.of("America/Sao_Paulo")
                );

        MonthlyFinancialReport report =
                service.create(
                        YearMonth.of(2026, 7)
                );

        assertEquals(9, report.totalDecisions());
        assertEquals(6, report.purchasedDecisions());
        assertEquals(2, report.declinedDecisions());
        assertEquals(1, report.waitingDecisions());

        assertEquals(
                Instant.parse("2026-07-01T03:00:00Z"),
                repository.receivedStart
        );

        assertEquals(
                Instant.parse("2026-08-01T03:00:00Z"),
                repository.receivedEnd
        );
    }

    @Test
    void shouldRejectNullMonth() {
        MonthlyFinancialReportService service =
                new MonthlyFinancialReportService(
                        new RecordingRepository(),
                        ZoneId.of("America/Sao_Paulo")
                );

        assertThrows(
                NullPointerException.class,
                () -> service.create(null)
        );
    }

    private static class RecordingRepository
            implements DashboardRepository {

        private Instant receivedStart;
        private Instant receivedEnd;

        @Override
        public DashboardSummary getSummary() {
            throw new UnsupportedOperationException();
        }

        @Override
        public DashboardWeeklySummary getWeeklySummary(
                Instant startInclusive,
                Instant endExclusive
        ) {
            receivedStart = startInclusive;
            receivedEnd = endExclusive;

            return new DashboardWeeklySummary(
                    9,
                    6,
                    2,
                    new BigDecimal("457.00"),
                    new BigDecimal("100.00"),
                    3342
            );
        }
    }
}