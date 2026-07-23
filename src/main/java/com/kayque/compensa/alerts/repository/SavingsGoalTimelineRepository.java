package com.kayque.compensa.alerts.repository;

import java.time.LocalDate;
import java.util.Optional;

public interface SavingsGoalTimelineRepository {

    Optional<LocalDate> findCreatedDate();
}