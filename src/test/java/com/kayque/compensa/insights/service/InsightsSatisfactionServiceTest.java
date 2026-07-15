package com.kayque.compensa.insights.service;

import com.kayque.compensa.insights.model.InsightReport;
import com.kayque.compensa.insights.model.InsightsSummary;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InsightsSatisfactionServiceTest {

    private final InsightsService service =
            new InsightsService();

    @Test
    void shouldCalculatePostPurchaseRates() {
        InsightsSummary summary = new InsightsSummary(
                10,
                5,
                3,
                2,
                1,
                600,
                4,
                2,
                1,
                1
        );

        InsightReport report =
                service.generateReport(summary);

        assertEquals(4, report.evaluatedPurchases());
        assertEquals(50, report.satisfactionRate());
        assertEquals(25, report.regretRate());
    }

    @Test
    void shouldReturnZeroRatesWithoutEvaluations() {
        InsightsSummary summary = new InsightsSummary(
                5,
                2,
                2,
                1,
                0,
                300
        );

        InsightReport report =
                service.generateReport(summary);

        assertEquals(0, report.evaluatedPurchases());
        assertEquals(0, report.satisfactionRate());
        assertEquals(0, report.regretRate());
    }
}