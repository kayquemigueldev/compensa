package com.kayque.compensa.alerts.service;

import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.model.SmartAlertPriority;
import com.kayque.compensa.alerts.model.SmartAlertSnapshot;
import com.kayque.compensa.alerts.model.SmartAlertTopic;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmartAlertEngineFactoryTest {

    @Test
    void shouldCreateDefaultAlertEngine() {
        SmartAlertEngine engine =
                SmartAlertEngineFactory.createDefault();

        assertNotNull(engine);
    }

    @Test
    void shouldGenerateAlertsFromEveryDefaultRule() {
        SmartAlertEngine engine =
                SmartAlertEngineFactory.createDefault();

        List<SmartAlert> alerts =
                engine.generate(completeSnapshot());

        Set<SmartAlertTopic> generatedTopics =
                alerts.stream()
                        .map(SmartAlert::topic)
                        .collect(Collectors.toSet());

        assertEquals(
                EnumSet.allOf(SmartAlertTopic.class),
                generatedTopics
        );

        assertEquals(
                SmartAlertTopic.values().length,
                alerts.size()
        );
    }

    @Test
    void shouldNotGenerateContradictoryTopics() {
        SmartAlertEngine engine =
                SmartAlertEngineFactory.createDefault();

        List<SmartAlert> alerts =
                engine.generate(completeSnapshot());

        long distinctTopics = alerts.stream()
                .map(SmartAlert::topic)
                .distinct()
                .count();

        assertEquals(
                alerts.size(),
                distinctTopics
        );
    }

    @Test
    void shouldOrderAlertsByPriority() {
        SmartAlertEngine engine =
                SmartAlertEngineFactory.createDefault();

        List<SmartAlert> alerts =
                engine.generate(completeSnapshot());

        for (int index = 1;
             index < alerts.size();
             index++) {

            SmartAlertPriority previous =
                    alerts.get(index - 1).priority();

            SmartAlertPriority current =
                    alerts.get(index).priority();

            assertTrue(
                    previous.weight()
                            >= current.weight()
            );
        }
    }

    private SmartAlertSnapshot completeSnapshot() {
        return new SmartAlertSnapshot(
                new BigDecimal("95"),
                new BigDecimal("25"),
                new BigDecimal("500"),
                new BigDecimal("500"),
                new BigDecimal("70"),
                new BigDecimal("50"),
                2,
                5,
                3,
                1,
                2400,
                new BigDecimal("2350")
        );
    }
}