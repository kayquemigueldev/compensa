package com.kayque.compensa.alerts.controller;

import com.kayque.compensa.alerts.repository.SqliteSmartAlertSnoozeRepository;
import com.kayque.compensa.alerts.service.SmartAlertService;
import com.kayque.compensa.alerts.service.SmartAlertServiceFactory;
import com.kayque.compensa.alerts.service.SmartAlertSnoozeService;
import com.kayque.compensa.dashboard.model.DashboardSmartAlertView;
import com.kayque.compensa.dashboard.service.DashboardSmartAlertPresentationService;
import com.kayque.compensa.navigation.NavigationRequestEvent;
import com.kayque.compensa.navigation.NavigationTarget;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.Clock;
import java.time.Duration;
import java.util.List;

public class AlertCenterController {

    private final SmartAlertService smartAlertService =
            SmartAlertServiceFactory.createDefault();

    private final SmartAlertSnoozeService
            smartAlertSnoozeService =
            new SmartAlertSnoozeService(
                    new SqliteSmartAlertSnoozeRepository(),
                    Clock.systemDefaultZone()
            );

    private final DashboardSmartAlertPresentationService
            presentationService =
            new DashboardSmartAlertPresentationService();

    @FXML
    private Label alertCountLabel;

    @FXML
    private VBox alertListContainer;

    @FXML
    private Label emptyStateLabel;

    @FXML
    private void initialize() {
        loadAlerts();
    }

    private void loadAlerts() {
        try {
            List<DashboardSmartAlertView> alerts =
                    presentationService.prepareAll(
                            smartAlertSnoozeService.filterVisible(
                                    smartAlertService.generateAlerts()
                            )
                    );

            renderAlerts(alerts);

        } catch (RuntimeException exception) {
            alertListContainer
                    .getChildren()
                    .clear();

            alertCountLabel.setText(
                    "Não foi possível carregar"
            );

            showEmptyState(
                    "O Compensa? não conseguiu carregar seus alertas agora."
            );
        }
    }

    private void renderAlerts(
            List<DashboardSmartAlertView> alerts
    ) {
        alertListContainer
                .getChildren()
                .clear();

        updateAlertCount(alerts.size());

        boolean empty = alerts.isEmpty();

        emptyStateLabel.setVisible(empty);
        emptyStateLabel.setManaged(empty);

        if (empty) {
            showEmptyState(
                    "Nenhum alerta ativo neste momento. Está tudo tranquilo por aqui."
            );

            return;
        }

        alerts.stream()
                .map(this::createAlertCard)
                .forEach(card ->
                        alertListContainer
                                .getChildren()
                                .add(card)
                );
    }

    private VBox createAlertCard(
            DashboardSmartAlertView alert
    ) {
        Label titleLabel = new Label(
                alert.title()
        );

        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        titleLabel.getStyleClass().add(
                "dashboard-smart-alert-title"
        );

        Label messageLabel = new Label(
                alert.message()
        );

        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(Double.MAX_VALUE);

        messageLabel.getStyleClass().add(
                "dashboard-smart-alert-message"
        );

        Label explanationLabel = new Label(
                alert.explanation()
        );

        explanationLabel.setWrapText(true);
        explanationLabel.setMaxWidth(Double.MAX_VALUE);
        explanationLabel.setVisible(false);
        explanationLabel.setManaged(false);

        explanationLabel.getStyleClass().add(
                "dashboard-smart-alert-explanation"
        );

        Button explanationButton =
                createExplanationButton(
                        explanationLabel
                );

        Button snoozeButton =
                createSnoozeButton(alert);

        HBox buttons = new HBox(
                8,
                explanationButton,
                snoozeButton
        );

        buttons.getStyleClass().add(
                "dashboard-smart-alert-buttons"
        );

        Region verticalSpace = new Region();

        VBox.setVgrow(
                verticalSpace,
                Priority.ALWAYS
        );

        VBox card = new VBox(
                8,
                titleLabel,
                messageLabel,
                buttons,
                explanationLabel,
                verticalSpace
        );

        if (alert.hasNavigation()) {
            Button navigationButton =
                    createNavigationButton(alert);

            card.getChildren().add(
                    navigationButton
            );
        }

        card.setMinWidth(0);
        card.setMaxWidth(Double.MAX_VALUE);

        card.getStyleClass().addAll(
                "dashboard-smart-alert-card",
                "alert-center-card",
                alert.styleClass()
        );

        return card;
    }

    private Button createExplanationButton(
            Label explanationLabel
    ) {
        Button button = new Button(
                "Entender este alerta"
        );

        button.getStyleClass().add(
                "dashboard-smart-alert-explanation-button"
        );

        button.setOnAction(event -> {
            boolean show =
                    !explanationLabel.isManaged();

            explanationLabel.setVisible(show);
            explanationLabel.setManaged(show);

            button.setText(
                    show
                            ? "Ocultar explicação"
                            : "Entender este alerta"
            );
        });

        return button;
    }

    private Button createSnoozeButton(
            DashboardSmartAlertView alert
    ) {
        Button button = new Button(
                "Lembrar depois"
        );

        button.getStyleClass().add(
                "dashboard-smart-alert-snooze-button"
        );

        button.setOnAction(event -> {
            smartAlertSnoozeService.snooze(
                    alert.code(),
                    Duration.ofHours(24)
            );

            loadAlerts();
        });

        return button;
    }

    private Button createNavigationButton(
            DashboardSmartAlertView alert
    ) {
        Button button = new Button(
                getNavigationText(
                        alert.navigationTarget()
                )
        );

        button.setMaxWidth(Double.MAX_VALUE);

        button.getStyleClass().add(
                "alert-center-navigation-button"
        );

        button.setOnAction(event ->
                alertListContainer.fireEvent(
                        new NavigationRequestEvent(
                                alert.navigationTarget()
                        )
                )
        );

        return button;
    }

    private String getNavigationText(
            NavigationTarget target
    ) {
        return switch (target) {
            case FINANCIAL_PROFILE ->
                    "Abrir perfil financeiro →";

            case SAVINGS_GOAL ->
                    "Abrir meu objetivo →";

            case HISTORY ->
                    "Revisar histórico →";

            case INSIGHTS ->
                    "Ver insights →";

            case ALERT_CENTER ->
                    "Abrir central de alertas →";

            case NONE ->
                    "Sem ação disponível";
        };
    }

    private void updateAlertCount(int total) {
        if (total == 1) {
            alertCountLabel.setText(
                    "1 alerta ativo"
            );

            return;
        }

        alertCountLabel.setText(
                total + " alertas ativos"
        );
    }

    private void showEmptyState(String message) {
        emptyStateLabel.setText(message);
        emptyStateLabel.setVisible(true);
        emptyStateLabel.setManaged(true);
    }
}