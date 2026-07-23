package com.kayque.compensa.settings.controller;

import com.kayque.compensa.database.DatabaseConnection;
import com.kayque.compensa.settings.service.DatabaseBackupService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.DialogPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;


import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

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
                chooser.showDialog(getWindow());

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

    @FXML
    private void restoreBackup() {
        clearBackupFeedback();

        FileChooser chooser = new FileChooser();

        chooser.setTitle(
                "Escolha um backup do Compensa?"
        );

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Backup do Compensa? (*.db)",
                        "*.db"
                )
        );

        File selectedFile =
                chooser.showOpenDialog(getWindow());

        if (selectedFile == null) {
            return;
        }

        if (!confirmRestore(selectedFile)) {
            return;
        }

        try {
            Path safetyBackup =
                    backupService.restoreBackup(
                            selectedFile.toPath(),
                            DatabaseConnection.getDatabaseFile(),
                            LocalDateTime.now()
                    );

            showBackupSuccess(
                    "Backup restaurado com sucesso. "
                            + "Reinicie o Compensa? para carregar os dados. "
                            + "Uma cópia de segurança do banco anterior foi criada em: "
                            + safetyBackup.getFileName()
            );

        } catch (IllegalArgumentException
                 | IllegalStateException exception) {
            showBackupError(exception.getMessage());
        }
    }

    private boolean confirmRestore(File selectedFile) {
        ButtonType restoreButton = new ButtonType(
                "Restaurar",
                ButtonBar.ButtonData.OK_DONE
        );

        ButtonType cancelButton = new ButtonType(
                "Cancelar",
                ButtonBar.ButtonData.CANCEL_CLOSE
        );

        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION,
                "",
                restoreButton,
                cancelButton
        );

        confirmation.initOwner(getWindow());

        confirmation.setTitle(
                "Confirmar restauração"
        );

        confirmation.setHeaderText(
                "Restaurar este backup?"
        );

        confirmation.setContentText(
                "Arquivo selecionado:\n"
                        + selectedFile.getName()
                        + "\n\nOs dados atuais serão substituídos. "
                        + "Antes disso, o Compensa? criará automaticamente "
                        + "uma cópia de segurança do banco atual."
        );

        confirmation.setGraphic(null);

        DialogPane dialogPane =
                confirmation.getDialogPane();

        dialogPane.getStylesheets().add(
                SettingsController.class
                        .getResource(
                                "/com/kayque/compensa/styles.css"
                        )
                        .toExternalForm()
        );

        dialogPane.getStyleClass().add(
                "compensa-dialog"
        );

        Optional<ButtonType> selectedButton =
                confirmation.showAndWait();

        return selectedButton.isPresent()
                && selectedButton.get() == restoreButton;
    }

    private Window getWindow() {
        return applicationVersionLabel
                .getScene()
                .getWindow();
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