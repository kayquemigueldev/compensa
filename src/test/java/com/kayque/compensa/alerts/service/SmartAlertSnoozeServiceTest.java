package com.kayque.compensa.alerts.service;

import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.model.SmartAlertPriority;
import com.kayque.compensa.alerts.model.SmartAlertSnooze;
import com.kayque.compensa.alerts.model.SmartAlertTopic;
import com.kayque.compensa.alerts.repository.SmartAlertSnoozeRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SmartAlertSnoozeServiceTest {

    private static final Instant CURRENT_INSTANT =
            Instant.parse("2026-07-26T15:00:00Z");

    private final InMemorySnoozeRepository repository =
            new InMemorySnoozeRepository();

    private final SmartAlertSnoozeService service =
            new SmartAlertSnoozeService(
                    repository,
                    Clock.fixed(
                            CURRENT_INSTANT,
                            ZoneId.of("America/Sao_Paulo")
                    )
            );

    @Test
    void shouldHideSnoozedAlert() {
        SmartAlert alert = createAlert("budget-usage");

        service.snooze(
                alert,
                Duration.ofHours(24)
        );

        List<SmartAlert> visibleAlerts =
                service.filterVisible(List.of(alert));

        assertEquals(0, visibleAlerts.size());
    }

    @Test
    void shouldKeepAlertWithoutSnoozeVisible() {
        SmartAlert alert =
                createAlert("purchase-behavior");

        List<SmartAlert> visibleAlerts =
                service.filterVisible(List.of(alert));

        assertEquals(
                List.of(alert),
                visibleAlerts
        );
    }

    @Test
    void shouldRejectNonPositiveDuration() {
        SmartAlert alert = createAlert("budget-usage");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.snooze(
                        alert,
                        Duration.ZERO
                )
        );
    }

    @Test
    void shouldListActiveSnoozes() {
        service.snooze(
                "budget-usage",
                Duration.ofHours(24)
        );

        service.snooze(
                "purchase-behavior",
                Duration.ofHours(12)
        );

        List<SmartAlertSnooze> snoozes =
                service.findActive();

        assertEquals(2, snoozes.size());

        assertEquals(
                "purchase-behavior",
                snoozes.getFirst().alertCode()
        );

        assertEquals(
                "budget-usage",
                snoozes.getLast().alertCode()
        );
    }

    @Test
    void shouldRestoreSpecificAlert() {
        service.snooze(
                "budget-usage",
                Duration.ofHours(24)
        );

        service.snooze(
                "purchase-behavior",
                Duration.ofHours(24)
        );

        service.restore("budget-usage");

        List<SmartAlertSnooze> snoozes =
                service.findActive();

        assertEquals(1, snoozes.size());

        assertEquals(
                "purchase-behavior",
                snoozes.getFirst().alertCode()
        );
    }

    @Test
    void shouldRestoreAllAlerts() {
        service.snooze(
                "budget-usage",
                Duration.ofHours(24)
        );

        service.snooze(
                "purchase-behavior",
                Duration.ofHours(24)
        );

        service.restoreAll();

        assertEquals(
                List.of(),
                service.findActive()
        );
    }

    private SmartAlert createAlert(String code) {
        return new SmartAlert(
                code,
                SmartAlertTopic.BUDGET_USAGE,
                SmartAlertPriority.ATTENTION,
                "Alerta de teste",
                "Mensagem de teste"
        );
    }

    private static class InMemorySnoozeRepository
            implements SmartAlertSnoozeRepository {

        private final Map<String, Instant> snoozes =
                new HashMap<>();

        @Override
        public void save(
                String alertCode,
                Instant snoozedUntil
        ) {
            snoozes.put(
                    alertCode,
                    snoozedUntil
            );
        }

        @Override
        public boolean isSnoozed(
                String alertCode,
                Instant currentInstant
        ) {
            Instant snoozedUntil =
                    snoozes.get(alertCode);

            return snoozedUntil != null
                    && snoozedUntil.isAfter(
                    currentInstant
            );
        }

        @Override
        public List<SmartAlertSnooze> findActive(
                Instant currentInstant
        ) {
            return snoozes.entrySet()
                    .stream()
                    .filter(entry ->
                            entry.getValue().isAfter(
                                    currentInstant
                            )
                    )
                    .map(entry ->
                            new SmartAlertSnooze(
                                    entry.getKey(),
                                    entry.getValue()
                            )
                    )
                    .sorted(
                            Comparator.comparing(
                                    SmartAlertSnooze::snoozedUntil
                            )
                    )
                    .toList();
        }

        @Override
        public void delete(String alertCode) {
            snoozes.remove(alertCode);
        }

        @Override
        public void deleteAll() {
            snoozes.clear();
        }

        @Override
        public void deleteExpired(
                Instant currentInstant
        ) {
            snoozes.entrySet().removeIf(entry ->
                    !entry.getValue().isAfter(
                            currentInstant
                    )
            );
        }
    }
}