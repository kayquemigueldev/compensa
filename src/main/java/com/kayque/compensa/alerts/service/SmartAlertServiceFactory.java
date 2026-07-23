package com.kayque.compensa.alerts.service;

public final class SmartAlertServiceFactory {

    private SmartAlertServiceFactory() {
    }

    public static SmartAlertService createDefault() {
        return new SmartAlertService(
                SmartAlertSnapshotProviderFactory
                        .createDefault(),

                SmartAlertEngineFactory
                        .createDefault()
        );
    }
}