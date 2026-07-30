package com.kayque.compensa.settings.controller;

import com.kayque.compensa.ApplicationInfo;
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
import javafx.application.Platform;

import com.kayque.compensa.onboarding.event.OnboardingRequestedEvent;
import com.kayque.compensa.alerts.model.SmartAlertSnooze;
import com.kayque.compensa.alerts.repository.SqliteSmartAlertSnoozeRepository;
import com.kayque.compensa.alerts.service.SmartAlertSnoozeService;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

public class SettingsController {

    private final DatabaseBackupService backupService =
            new DatabaseBackupService();

    private final SmartAlertSnoozeService snoozeService =
            new SmartAlertSnoozeService(
                    new SqliteSmartAlertSnoozeRepository(),
                    Clock.systemDefaultZone()
            );

    private final DateTimeFormatter snoozeDateFormat =
            DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy 'às' HH:mm"
            );

    @FXML
    private Label applicationVersionLabel;

    @FXML
    private Label backupFeedbackLabel;

    @FXML
    private VBox snoozedAlertsContainer;

    @FXML
    private Label snoozedAlertsEmptyLabel;

    @FXML
    private Label snoozeFeedbackLabel;

    @FXML
    private Button restoreAllSnoozesButton;

    @FXML
    private void initialize() {
        applicationVersionLabel.setText(
                ApplicationInfo.VERSION
        );

        loadSnoozedAlerts();
    }

    private void loadSnoozedAlerts() {
        clearSnoozeFeedback();

        try {
            List<SmartAlertSnooze> snoozes =
                    snoozeService.findActive();

            renderSnoozedAlerts(snoozes);

        } catch (IllegalStateException exception) {
            snoozedAlertsContainer
                    .getChildren()
                    .clear();

            snoozedAlertsEmptyLabel.setText(
                    "Não foi possível carregar os alertas adiados."
            );

            snoozedAlertsEmptyLabel.setVisible(true);
            snoozedAlertsEmptyLabel.setManaged(true);

            restoreAllSnoozesButton.setDisable(true);
        }
    }

    private void renderSnoozedAlerts(
            List<SmartAlertSnooze> snoozes
    ) {
        snoozedAlertsContainer
                .getChildren()
                .clear();

        boolean empty = snoozes.isEmpty();

        snoozedAlertsEmptyLabel.setText(
                "Nenhum alerta está temporariamente oculto."
        );

        snoozedAlertsEmptyLabel.setVisible(empty);
        snoozedAlertsEmptyLabel.setManaged(empty);

        restoreAllSnoozesButton.setDisable(empty);

        snoozes.stream()
                .map(this::createSnoozedAlertRow)
                .forEach(row ->
                        snoozedAlertsContainer
                                .getChildren()
                                .add(row)
                );
    }

    private HBox createSnoozedAlertRow(
            SmartAlertSnooze snooze
    ) {
        Label titleLabel = new Label(
                formatAlertName(snooze.alertCode())
        );

        titleLabel.getStyleClass().add(
                "settings-snooze-title"
        );

        Label expirationLabel = new Label(
                "Voltará automaticamente em "
                        + formatSnoozeExpiration(
                        snooze.snoozedUntil()
                )
        );

        expirationLabel.getStyleClass().add(
                "settings-snooze-expiration"
        );

        VBox information = new VBox(
                4,
                titleLabel,
                expirationLabel
        );

        Region spacer = new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Button restoreButton =
                new Button("Restaurar agora");

        restoreButton.getStyleClass().add(
                "settings-snooze-restore-button"
        );

        restoreButton.setOnAction(event ->
                restoreSnoozedAlert(
                        snooze.alertCode()
                )
        );

        HBox row = new HBox(
                14,
                information,
                spacer,
                restoreButton
        );

        row.setAlignment(
                javafx.geometry.Pos.CENTER_LEFT
        );

        row.getStyleClass().add(
                "settings-snooze-row"
        );

        return row;
    }

    private void restoreSnoozedAlert(
            String alertCode
    ) {
        clearSnoozeFeedback();

        try {
            snoozeService.restore(alertCode);
            loadSnoozedAlerts();

            showSnoozeSuccess(
                    "Alerta restaurado. Ele poderá aparecer novamente na tela Hoje."
            );

        } catch (IllegalArgumentException
                 | IllegalStateException exception) {
            showSnoozeError(exception.getMessage());
        }
    }

    @FXML
    private void restoreAllSnoozedAlerts() {
        clearSnoozeFeedback();

        try {
            snoozeService.restoreAll();
            loadSnoozedAlerts();

            showSnoozeSuccess(
                    "Todos os alertas adiados foram restaurados."
            );

        } catch (IllegalStateException exception) {
            showSnoozeError(
                    "Não foi possível restaurar os alertas."
            );
        }
    }

    private String formatSnoozeExpiration(
            Instant snoozedUntil
    ) {
        return snoozedUntil
                .atZone(ZoneId.systemDefault())
                .format(snoozeDateFormat);
    }

    private String formatAlertName(String alertCode) {
        return switch (alertCode) {
            case "budget-usage" ->
                    "Uso do orçamento mensal";

            case "monthly-savings-goal" ->
                    "Meta mensal de economia";

            case "financial-goal-progress" ->
                    "Progresso do objetivo financeiro";

            case "pending-decisions" ->
                    "Decisões aguardando resposta";

            case "purchase-evaluation" ->
                    "Compras aguardando avaliação";

            case "purchase-behavior" ->
                    "Comportamento de compra";

            case "work-time" ->
                    "Tempo de trabalho envolvido";

            case "preserved-value" ->
                    "Valor preservado";

            default ->
                    "Alerta inteligente";
        };
    }

    private void clearSnoozeFeedback() {
        snoozeFeedbackLabel.setText("");

        snoozeFeedbackLabel.getStyleClass().setAll(
                "feedback-label"
        );
    }

    private void showSnoozeSuccess(String message) {
        snoozeFeedbackLabel.setText(message);

        snoozeFeedbackLabel.getStyleClass().setAll(
                "feedback-label",
                "feedback-success"
        );
    }

    private void showSnoozeError(String message) {
        snoozeFeedbackLabel.setText(message);

        snoozeFeedbackLabel.getStyleClass().setAll(
                "feedback-label",
                "feedback-error"
        );
    }

    @FXML
    private void showOnboardingAgain() {
        applicationVersionLabel.fireEvent(
                new OnboardingRequestedEvent()
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

            showRestoreCompleted(safetyBackup);

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

        applyDialogStyle(
                confirmation.getDialogPane()
        );

        Optional<ButtonType> selectedButton =
                confirmation.showAndWait();

        return selectedButton.isPresent()
                && selectedButton.get() == restoreButton;
    }

    private void showRestoreCompleted(
            Path safetyBackup
    ) {
        ButtonType closeButton = new ButtonType(
                "Fechar aplicativo",
                ButtonBar.ButtonData.OK_DONE
        );

        Alert completedAlert = new Alert(
                Alert.AlertType.INFORMATION,
                "",
                closeButton
        );

        completedAlert.initOwner(getWindow());

        completedAlert.setTitle(
                "Restauração concluída"
        );

        completedAlert.setHeaderText(
                "Seus dados foram restaurados"
        );

        completedAlert.setContentText(
                "O backup foi restaurado com sucesso.\n\n"
                        + "Uma cópia de segurança dos dados anteriores "
                        + "foi criada em:\n"
                        + safetyBackup.getFileName()
                        + "\n\nO Compensa? será fechado para carregar "
                        + "os dados restaurados com segurança na próxima abertura."
        );

        completedAlert.setGraphic(null);

        applyDialogStyle(
                completedAlert.getDialogPane()
        );

        completedAlert.showAndWait();

        Platform.exit();
    }

    private void applyDialogStyle(
            DialogPane dialogPane
    ) {
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