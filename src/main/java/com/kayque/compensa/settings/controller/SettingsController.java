package com.kayque.compensa.settings.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SettingsController {

    private static final String APPLICATION_VERSION =
            "1.0.0";

    @FXML
    private Label applicationVersionLabel;

    @FXML
    private void initialize() {
        applicationVersionLabel.setText(
                APPLICATION_VERSION
        );
    }
}