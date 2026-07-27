package com.kayque.compensa.alerts.service;

import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.model.SmartAlertPriority;
import com.kayque.compensa.alerts.model.SmartAlertTopic;
import com.kayque.compensa.alerts.repository.SmartAlertReadRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmartAlertReadServiceTest {

    private static final Instant CURRENT_INSTANT =
            Instant.parse("2026-07-27T15:00:00Z");

    private final FakeSmartAlertReadRepository repository =
            new FakeSmartAlertReadRepository();

    private final SmartAlertReadService service =
            new SmartAlertReadService(
                    repository,
                    Clock.fixed(
                            CURRENT_INSTANT,
                            ZoneOffset.UTC
                    )
            );

    @Test
    void shouldMarkAlertAsRead() {
        SmartAlert alert = createAlert("budget.attention");

        service.markAsRead(alert);

        assertTrue(service.isRead(alert));
        assertEquals(
                CURRENT_INSTANT,
                repository.readStates.get(alert.code())
        );
    }

    @Test
    void shouldReturnOnlyUnreadAlerts() {
        SmartAlert first =
                createAlert("budget.attention");

        SmartAlert second =
                createAlert("goal.progress");

        service.markAsRead(first);

        List<SmartAlert> unread =
                service.filterUnread(
                        List.of(first, second)
                );

        assertEquals(List.of(second), unread);
    }

    @Test
    void shouldCountUnreadAlerts() {
        SmartAlert first =
                createAlert("budget.attention");

        SmartAlert second =
                createAlert("goal.progress");

        service.markAsRead(first);

        assertEquals(
                1,
                service.countUnread(
                        List.of(first, second)
                )
        );
    }

    @Test
    void shouldForgetReadStateWhenAlertIsNoLongerActive() {
        SmartAlert oldAlert =
                createAlert("budget.attention");

        SmartAlert activeAlert =
                createAlert("goal.progress");

        service.markAsRead(oldAlert);

        service.synchronize(
                List.of(activeAlert)
        );

        assertFalse(
                repository.isRead(oldAlert.code())
        );
    }

    private SmartAlert createAlert(String code) {
        return new SmartAlert(
                code,
                SmartAlertTopic.WORK_TIME,
                SmartAlertPriority.ATTENTION,
                "Alerta de teste",
                "Mensagem de teste"
        );
    }

    private static class FakeSmartAlertReadRepository
            implements SmartAlertReadRepository {

        private final Map<String, Instant> readStates =
                new HashMap<>();

        @Override
        public void markAsRead(
                String alertCode,
                Instant readAt
        ) {
            readStates.put(alertCode, readAt);
        }

        @Override
        public boolean isRead(String alertCode) {
            return readStates.containsKey(alertCode);
        }

        @Override
        public Set<String> findAllReadCodes() {
            return Set.copyOf(readStates.keySet());
        }

        @Override
        public void delete(String alertCode) {
            readStates.remove(alertCode);
        }
    }
}