package com.kayque.compensa.alerts.event;

import javafx.event.Event;
import javafx.event.EventType;

public class SmartAlertStateChangedEvent extends Event {

    public static final EventType<SmartAlertStateChangedEvent>
            ALERT_STATE_CHANGED =
            new EventType<>(
                    Event.ANY,
                    "SMART_ALERT_STATE_CHANGED"
            );

    public SmartAlertStateChangedEvent() {
        super(ALERT_STATE_CHANGED);
    }
}