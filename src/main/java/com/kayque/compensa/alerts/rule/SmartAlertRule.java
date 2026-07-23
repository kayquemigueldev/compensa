package com.kayque.compensa.alerts.rule;

import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.model.SmartAlertSnapshot;

import java.util.Optional;

@FunctionalInterface
public interface SmartAlertRule {

    Optional<SmartAlert> evaluate(
            SmartAlertSnapshot snapshot
    );
}