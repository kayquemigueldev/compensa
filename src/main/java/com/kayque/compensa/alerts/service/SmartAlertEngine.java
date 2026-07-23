package com.kayque.compensa.alerts.service;

import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.model.SmartAlertSnapshot;
import com.kayque.compensa.alerts.model.SmartAlertTopic;
import com.kayque.compensa.alerts.rule.SmartAlertRule;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SmartAlertEngine {

    private final List<SmartAlertRule> rules;

    public SmartAlertEngine(
            List<SmartAlertRule> rules
    ) {
        Objects.requireNonNull(
                rules,
                "A lista de regras é obrigatória."
        );

        if (rules.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "A lista de regras não pode conter valores nulos."
            );
        }

        this.rules = List.copyOf(rules);
    }

    public List<SmartAlert> generate(
            SmartAlertSnapshot snapshot
    ) {
        Objects.requireNonNull(
                snapshot,
                "A fotografia financeira é obrigatória."
        );

        Map<SmartAlertTopic, SmartAlert> selectedAlerts =
                new EnumMap<>(SmartAlertTopic.class);

        for (SmartAlertRule rule : rules) {
            rule.evaluate(snapshot)
                    .ifPresent(alert ->
                            selectAlert(
                                    selectedAlerts,
                                    alert
                            )
                    );
        }

        List<SmartAlert> result =
                new ArrayList<>(
                        selectedAlerts.values()
                );

        result.sort(
                (first, second) -> {
                    int priorityComparison =
                            Integer.compare(
                                    second.priority().weight(),
                                    first.priority().weight()
                            );

                    if (priorityComparison != 0) {
                        return priorityComparison;
                    }

                    return first.code()
                            .compareTo(second.code());
                }
        );

        return List.copyOf(result);
    }

    private void selectAlert(
            Map<SmartAlertTopic, SmartAlert> selectedAlerts,
            SmartAlert candidate
    ) {
        SmartAlert current =
                selectedAlerts.get(candidate.topic());

        if (current == null
                || candidate.priority()
                .isHigherThan(current.priority())) {

            selectedAlerts.put(
                    candidate.topic(),
                    candidate
            );
        }
    }
}