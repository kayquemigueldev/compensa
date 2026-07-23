package com.kayque.compensa.alerts.service;

import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.model.SmartAlertSnapshot;

import java.util.List;
import java.util.Objects;

public class SmartAlertService {

    private final SmartAlertSnapshotSource snapshotSource;
    private final SmartAlertEngine engine;

    public SmartAlertService(
            SmartAlertSnapshotSource snapshotSource,
            SmartAlertEngine engine
    ) {
        this.snapshotSource = Objects.requireNonNull(
                snapshotSource,
                "A fonte dos dados financeiros é obrigatória."
        );

        this.engine = Objects.requireNonNull(
                engine,
                "O motor de alertas é obrigatório."
        );
    }

    public List<SmartAlert> generateAlerts() {
        SmartAlertSnapshot snapshot =
                snapshotSource.createSnapshot();

        return engine.generate(snapshot);
    }
}