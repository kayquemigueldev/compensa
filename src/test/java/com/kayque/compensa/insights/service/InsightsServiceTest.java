package com.kayque.compensa.insights.service;

import com.kayque.compensa.insights.model.InsightReport;
import com.kayque.compensa.insights.model.InsightsSummary;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InsightsServiceTest {

    private final InsightsService service =
            new InsightsService();

    @Test
    void shouldRequestMoreDataWhenHistoryIsSmall() {
        InsightsSummary summary = new InsightsSummary(
                1,
                1,
                0,
                0,
                1,
                48
        );

        InsightReport report =
                service.generateReport(summary);

        assertEquals(100, report.purchaseRate());
        assertEquals(0, report.declineRate());
        assertEquals(0, report.waitingRate());
        assertEquals(48, report.averageRealWorkMinutes());

        assertEquals(
                "Continue registrando suas decisões",
                report.headline()
        );
    }

    @Test
    void shouldDetectFrequentPurchasesAgainstAdvice() {
        InsightsSummary summary = new InsightsSummary(
                6,
                4,
                1,
                1,
                3,
                600
        );

        InsightReport report =
                service.generateReport(summary);

        assertEquals(67, report.purchaseRate());
        assertEquals(17, report.declineRate());
        assertEquals(17, report.waitingRate());
        assertEquals(100, report.averageRealWorkMinutes());

        assertEquals(
                "Observe as compras feitas após alertas",
                report.headline()
        );
    }
}