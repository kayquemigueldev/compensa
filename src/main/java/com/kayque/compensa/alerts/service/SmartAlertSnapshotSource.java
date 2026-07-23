package com.kayque.compensa.alerts.service;

import com.kayque.compensa.alerts.model.SmartAlertSnapshot;

@FunctionalInterface
public interface SmartAlertSnapshotSource {

    SmartAlertSnapshot createSnapshot();
}