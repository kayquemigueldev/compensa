package com.kayque.compensa.navigation;

import javafx.event.Event;
import javafx.event.EventType;

import java.util.Objects;

public class NavigationRequestEvent extends Event {

    public static final EventType<NavigationRequestEvent>
            NAVIGATION_REQUEST =
            new EventType<>(
                    Event.ANY,
                    "NAVIGATION_REQUEST"
            );

    private final NavigationTarget target;

    public NavigationRequestEvent(
            NavigationTarget target
    ) {
        super(NAVIGATION_REQUEST);

        this.target = Objects.requireNonNull(
                target,
                "O destino da navegação é obrigatório."
        );
    }

    public NavigationTarget target() {
        return target;
    }
}