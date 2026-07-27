package com.kayque.compensa.alerts.repository;

import java.time.Instant;
import java.util.Set;

public interface SmartAlertReadRepository {

    void markAsRead(
            String alertCode,
            Instant readAt
    );

    boolean isRead(String alertCode);

    Set<String> findAllReadCodes();

    void delete(String alertCode);
}