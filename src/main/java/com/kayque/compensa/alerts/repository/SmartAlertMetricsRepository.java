package com.kayque.compensa.alerts.repository;

import com.kayque.compensa.alerts.model.SmartAlertDecisionMetrics;

import java.time.Instant;

public interface SmartAlertMetricsRepository {

    SmartAlertDecisionMetrics getDecisionMetrics(
            Instant monthStartInclusive,
            Instant nextMonthStartExclusive,
            Instant yearStartInclusive,
            Instant nextYearStartExclusive,
            Instant overdueCutoffExclusive
    );
}