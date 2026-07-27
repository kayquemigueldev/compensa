package com.kayque.compensa.dashboard.repository;

import com.kayque.compensa.dashboard.model.DashboardSummary;
import com.kayque.compensa.dashboard.model.DashboardWeeklySummary;

import java.time.Instant;

public interface DashboardRepository {

    DashboardSummary getSummary();

    DashboardWeeklySummary getWeeklySummary(
            Instant startInclusive,
            Instant endExclusive
    );
}