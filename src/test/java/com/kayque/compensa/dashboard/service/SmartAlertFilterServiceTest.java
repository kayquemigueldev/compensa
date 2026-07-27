package com.kayque.compensa.alerts.service;

import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.model.SmartAlertFilter;
import com.kayque.compensa.alerts.model.SmartAlertFilterSummary;
import com.kayque.compensa.alerts.model.SmartAlertPriority;
import com.kayque.compensa.alerts.model.SmartAlertTopic;
import com.kayque.compensa.alerts.repository.SmartAlertReadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SmartAlertFilterServiceTest {

    private InMemoryReadRepository repository;
    private SmartAlertReadService readService;
    private SmartAlertFilterService filterService;

    private SmartAlert firstAlert;
    private SmartAlert secondAlert;

    @BeforeEach
    void setUp() {
        repository = new InMemoryReadRepository();

        Clock clock = Clock.fixed(
                Instant.parse("2026-07-27T12:00:00Z"),
                ZoneOffset.UTC
        );

        readService = new SmartAlertReadService(
                repository,
                clock
        );

        filterService = new SmartAlertFilterService(
                readService
        );

        firstAlert = createAlert(
                "budget.attention",
                "Atenção ao orçamento"
        );

        secondAlert = createAlert(
                "goal.progress",
                "Seu objetivo está avançando"
        );
    }

    @Test
    void shouldReturnAllAlerts() {
        List<SmartAlert> result = filterService.filter(
                List.of(firstAlert, secondAlert),
                SmartAlertFilter.ALL
        );

        assertEquals(
                List.of(firstAlert, secondAlert),
                result
        );
    }

    @Test
    void shouldReturnOnlyUnreadAlerts() {
        readService.markAsRead(firstAlert);

        List<SmartAlert> result = filterService.filter(
                List.of(firstAlert, secondAlert),
                SmartAlertFilter.UNREAD
        );

        assertEquals(
                List.of(secondAlert),
                result
        );
    }

    @Test
    void shouldReturnOnlyReadAlerts() {
        readService.markAsRead(firstAlert);

        List<SmartAlert> result = filterService.filter(
                List.of(firstAlert, secondAlert),
                SmartAlertFilter.READ
        );

        assertEquals(
                List.of(firstAlert),
                result
        );
    }

    @Test
    void shouldSummarizeAlertCounts() {
        readService.markAsRead(firstAlert);

        SmartAlertFilterSummary summary =
                filterService.summarize(
                        List.of(firstAlert, secondAlert)
                );

        assertEquals(2, summary.total());
        assertEquals(1, summary.unread());
        assertEquals(1, summary.read());
    }

    private SmartAlert createAlert(
            String code,
            String title
    ) {
        return new SmartAlert(
                code,
                SmartAlertTopic.BUDGET_USAGE,
                SmartAlertPriority.INFORMATIONAL,
                title,
                "Mensagem do alerta."
        );
    }

    private static class InMemoryReadRepository
            implements SmartAlertReadRepository {

        private final Set<String> readCodes =
                new HashSet<>();

        @Override
        public void markAsRead(
                String alertCode,
                Instant readAt
        ) {
            readCodes.add(alertCode);
        }

        @Override
        public boolean isRead(String alertCode) {
            return readCodes.contains(alertCode);
        }

        @Override
        public Set<String> findAllReadCodes() {
            return Set.copyOf(readCodes);
        }

        @Override
        public void delete(String alertCode) {
            readCodes.remove(alertCode);
        }
    }
}