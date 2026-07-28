package com.kayque.compensa.onboarding.event;

import javafx.event.Event;
import javafx.event.EventType;

public final class OnboardingRequestedEvent extends Event {

    public static final EventType<OnboardingRequestedEvent>
            ONBOARDING_REQUESTED =
            new EventType<>(
                    Event.ANY,
                    "ONBOARDING_REQUESTED"
            );

    public OnboardingRequestedEvent() {
        super(ONBOARDING_REQUESTED);
    }
}