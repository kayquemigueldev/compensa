package com.kayque.compensa.alerts.controller;

import com.kayque.compensa.alerts.repository.SqliteSmartAlertSnoozeRepository;
import com.kayque.compensa.alerts.service.SmartAlertService;
import com.kayque.compensa.alerts.service.SmartAlertServiceFactory;
import com.kayque.compensa.alerts.service.SmartAlertSnoozeService;
import com.kayque.compensa.dashboard.model.DashboardSmartAlertView;
import com.kayque.compensa.dashboard.service.DashboardSmartAlertPresentationService;
import com.kayque.compensa.navigation.NavigationRequestEvent;
import com.kayque.compensa.navigation.NavigationTarget;
import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.repository.SqliteSmartAlertReadRepository;
import com.kayque.compensa.alerts.service.SmartAlertReadService;
import com.kayque.compensa.alerts.event.SmartAlertStateChangedEvent;

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

    private final SmartAlertReadService
            smartAlertReadService =
            new SmartAlertReadService(
                    new SqliteSmartAlertReadRepository(),
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
    private Button allAlertsFilterButton;

    @FXML
    private Button unreadAlertsFilterButton;

    @FXML
    private Button readAlertsFilterButton;

    private AlertFilter selectedFilter =
            AlertFilter.ALL;

    private List<DashboardSmartAlertView> currentAlerts =
            List.of();

    @FXML
    private void initialize() {
        loadAlerts();
    }

    private void loadAlerts() {
        try {
            List<SmartAlert> generatedAlerts =
                    smartAlertService.generateAlerts();

            smartAlertReadService.synchronize(
                    generatedAlerts
            );

            List<SmartAlert> visibleAlerts =
                    smartAlertSnoozeService.filterVisible(
                            generatedAlerts
                    );

            currentAlerts =
                    presentationService.prepareAll(
                            visibleAlerts
                    );

            updateFilterInformation();
            renderSelectedFilter();

        } catch (RuntimeException exception) {
            currentAlerts = List.of();

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

    @FXML
    private void showAllAlerts() {
        selectFilter(AlertFilter.ALL);
    }

    @FXML
    private void showUnreadAlerts() {
        selectFilter(AlertFilter.UNREAD);
    }

    @FXML
    private void showReadAlerts() {
        selectFilter(AlertFilter.READ);
    }

    private void selectFilter(AlertFilter filter) {
        selectedFilter = filter;

        updateFilterButtonStyles();
        renderSelectedFilter();
    }

    private void updateFilterInformation() {
        int total = currentAlerts.size();
        int unreadTotal = countUnreadAlerts();
        int readTotal = total - unreadTotal;

        updateAlertCount(
                total,
                unreadTotal
        );

        allAlertsFilterButton.setText(
                "Todos (" + total + ")"
        );

        unreadAlertsFilterButton.setText(
                "Novos (" + unreadTotal + ")"
        );

        readAlertsFilterButton.setText(
                "Lidos (" + readTotal + ")"
        );

        updateFilterButtonStyles();
    }

    private int countUnreadAlerts() {
        return (int) currentAlerts
                .stream()
                .filter(alert ->
                        !smartAlertReadService.isRead(
                                alert.code()
                        )
                )
                .count();
    }

    private void renderSelectedFilter() {
        List<DashboardSmartAlertView> filteredAlerts =
                currentAlerts
                        .stream()
                        .filter(this::matchesSelectedFilter)
                        .toList();

        alertListContainer
                .getChildren()
                .clear();

        if (filteredAlerts.isEmpty()) {
            showEmptyState(
                    getEmptyStateMessage()
            );

            return;
        }

        emptyStateLabel.setVisible(false);
        emptyStateLabel.setManaged(false);

        filteredAlerts.stream()
                .map(this::createAlertCard)
                .forEach(card ->
                        alertListContainer
                                .getChildren()
                                .add(card)
                );
    }

    private boolean matchesSelectedFilter(
            DashboardSmartAlertView alert
    ) {
        boolean read =
                smartAlertReadService.isRead(
                        alert.code()
                );

        return switch (selectedFilter) {
            case ALL -> true;
            case UNREAD -> !read;
            case READ -> read;
        };
    }

    private String getEmptyStateMessage() {
        return switch (selectedFilter) {
            case ALL ->
                    "Nenhum alerta ativo neste momento. Está tudo tranquilo por aqui.";

            case UNREAD ->
                    "Nenhum alerta novo. Você já conferiu todos os sinais ativos.";

            case READ ->
                    "Nenhum alerta foi marcado como lido ainda.";
        };
    }

    private void updateFilterButtonStyles() {
        configureFilterButton(
                allAlertsFilterButton,
                selectedFilter == AlertFilter.ALL
        );

        configureFilterButton(
                unreadAlertsFilterButton,
                selectedFilter == AlertFilter.UNREAD
        );

        configureFilterButton(
                readAlertsFilterButton,
                selectedFilter == AlertFilter.READ
        );
    }

    private void configureFilterButton(
            Button button,
            boolean active
    ) {
        if (active) {
            button.getStyleClass().setAll(
                    "alert-center-filter-button",
                    "alert-center-filter-button-active"
            );

            return;
        }

        button.getStyleClass().setAll(
                "alert-center-filter-button"
        );
    }

    private VBox createAlertCard(
            DashboardSmartAlertView alert
    ) {
        boolean read =
                smartAlertReadService.isRead(
                        alert.code()
                );

        Label titleLabel = new Label(
                alert.title()
        );

        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(
                titleLabel,
                Priority.ALWAYS
        );

        titleLabel.getStyleClass().add(
                "dashboard-smart-alert-title"
        );

        Label readStatusLabel =
                createReadStatusLabel(read);

        HBox header = new HBox(
                10,
                titleLabel,
                readStatusLabel
        );

        header.setAlignment(
                javafx.geometry.Pos.CENTER_LEFT
        );

        header.getStyleClass().add(
                "alert-center-card-header"
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

        Button readButton =
                createReadButton(
                        alert,
                        read
                );

        HBox buttons = new HBox(
                8,
                explanationButton,
                snoozeButton,
                readButton
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
                header,
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

        if (read) {
            card.getStyleClass().add(
                    "alert-center-card-read"
            );
        }

        return card;
    }

    private Label createReadStatusLabel(
            boolean read
    ) {
        Label label = new Label(
                read ? "Lido" : "Novo"
        );

        label.getStyleClass().add(
                read
                        ? "alert-center-read-badge"
                        : "alert-center-new-badge"
        );

        return label;
    }

    private Button createReadButton(
            DashboardSmartAlertView alert,
            boolean read
    ) {
        Button button = new Button(
                read
                        ? "Alerta lido"
                        : "Marcar como lido"
        );

        button.getStyleClass().add(
                "alert-center-read-button"
        );

        button.setDisable(read);

        if (!read) {
            button.setOnAction(event -> {
                smartAlertReadService.markAsRead(
                        alert.code()
                );

                notifyAlertStateChanged();
                loadAlerts();
            });
        }

        return button;
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

            notifyAlertStateChanged();
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

    private void updateAlertCount(
            int total,
            int unreadTotal
    ) {
        String activeText =
                total == 1
                        ? "1 alerta ativo"
                        : total + " alertas ativos";

        String unreadText =
                unreadTotal == 1
                        ? "1 novo"
                        : unreadTotal + " novos";

        alertCountLabel.setText(
                activeText
                        + " • "
                        + unreadText
        );
    }

    private void showEmptyState(String message) {
        emptyStateLabel.setText(message);
        emptyStateLabel.setVisible(true);
        emptyStateLabel.setManaged(true);

        if (!alertListContainer
                .getChildren()
                .contains(emptyStateLabel)) {

            alertListContainer
                    .getChildren()
                    .add(emptyStateLabel);
        }
    }

    private void notifyAlertStateChanged() {
        alertListContainer.fireEvent(
                new SmartAlertStateChangedEvent()
        );
    }

    private enum AlertFilter {
        ALL,
        UNREAD,
        READ
    }

}