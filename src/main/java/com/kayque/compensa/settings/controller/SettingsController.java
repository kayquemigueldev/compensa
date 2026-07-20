package com.kayque.compensa.settings.controller;

import com.kayque.compensa.database.DatabaseConnection;
import com.kayque.compensa.settings.service.DatabaseBackupService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class SettingsController {

    private static final String APPLICATION_VERSION =
            "1.0.0";

    private final DatabaseBackupService backupService =
            new DatabaseBackupService();

    @FXML
    private Label applicationVersionLabel;

    @FXML
    private Label backupFeedbackLabel;

    @FXML
    private void initialize() {
        applicationVersionLabel.setText(
                APPLICATION_VERSION
        );
    }

    @FXML
    private void createBackup() {
        clearBackupFeedback();

        DirectoryChooser chooser =
                new DirectoryChooser();

        chooser.setTitle(
                "Escolha onde salvar o backup do Compensa?"
        );

        File selectedDirectory =
                chooser.showDialog(
                        applicationVersionLabel
                                .getScene()
                                .getWindow()
                );

        if (selectedDirectory == null) {
            return;
        }

        try {
            Path backupFile =
                    backupService.createBackup(
                            DatabaseConnection.getDatabaseFile(),
                            selectedDirectory.toPath(),
                            LocalDateTime.now()
                    );

            showBackupSuccess(
                    "Backup criado com sucesso: "
                            + backupFile.getFileName()
            );

        } catch (IllegalArgumentException
                 | IllegalStateException exception) {
            showBackupError(exception.getMessage());
        }
    }

    private void clearBackupFeedback() {
        backupFeedbackLabel.setText("");

        backupFeedbackLabel.getStyleClass().setAll(
                "feedback-label"
        );
    }

    private void showBackupSuccess(String message) {
        backupFeedbackLabel.setText(message);

        backupFeedbackLabel.getStyleClass().setAll(
                "feedback-label",
                "feedback-success"
        );
    }

    private void showBackupError(String message) {
        backupFeedbackLabel.setText(message);

        backupFeedbackLabel.getStyleClass().setAll(
                "feedback-label",
                "feedback-error"
        );
    }
}