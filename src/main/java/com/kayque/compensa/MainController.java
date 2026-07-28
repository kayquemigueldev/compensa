package com.kayque.compensa;

import com.kayque.compensa.alerts.event.SmartAlertStateChangedEvent;
import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.repository.SqliteSmartAlertReadRepository;
import com.kayque.compensa.alerts.repository.SqliteSmartAlertSnoozeRepository;
import com.kayque.compensa.alerts.service.SmartAlertReadService;
import com.kayque.compensa.alerts.service.SmartAlertService;
import com.kayque.compensa.alerts.service.SmartAlertServiceFactory;
import com.kayque.compensa.alerts.service.SmartAlertSnoozeService;
import com.kayque.compensa.navigation.NavigationRequestEvent;
import com.kayque.compensa.navigation.NavigationTarget;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.time.Clock;
import java.util.List;

public class MainController {

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

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private Button analyzePurchaseButton;

    @FXML
    private Button historyButton;

    @FXML
    private Button financialProfileButton;

    @FXML
    private Button wishlistButton;

    @FXML
    private Button todayButton;

    @FXML
    private Button insightsButton;

    @FXML
    private Button monthlyReportButton;

    @FXML
    private Button alertCenterButton;

    @FXML
    private Button settingsButton;

    @FXML
    private Button userProfileButton;

    @FXML
    private Button savingsGoalButton;

    @FXML
    private Label alertBadgeLabel;

    @FXML
    private ScrollPane mainContentScrollPane;

    private Node purchaseAnalysisView;

    @FXML
    private void initialize() {
        purchaseAnalysisView =
                mainContentScrollPane.getContent();

        mainBorderPane.addEventHandler(
                NavigationRequestEvent.NAVIGATION_REQUEST,
                this::handleNavigationRequest
        );

        mainBorderPane.addEventHandler(
                SmartAlertStateChangedEvent.ALERT_STATE_CHANGED,
                event -> refreshAlertBadge()
        );

        refreshAlertBadge();
    }

    @FXML
    private void showDashboard() {
        showView(
                "/com/kayque/compensa/dashboard/dashboard-view.fxml",
                todayButton,
                "Não foi possível abrir a tela Hoje."
        );
    }

    @FXML
    private void showPurchaseAnalysis() {
        showContent(purchaseAnalysisView);
        setActiveButton(analyzePurchaseButton);
    }

    @FXML
    private void showWishlist() {
        showView(
                "/com/kayque/compensa/wishlist/wishlist-view.fxml",
                wishlistButton,
                "Não foi possível abrir a lista de desejos."
        );
    }

    @FXML
    private void showHistory() {
        showView(
                "/com/kayque/compensa/history/history-view.fxml",
                historyButton,
                "Não foi possível abrir o histórico."
        );
    }

    @FXML
    private void showInsights() {
        showView(
                "/com/kayque/compensa/insights/insights-view.fxml",
                insightsButton,
                "Não foi possível abrir os insights."
        );
    }

    @FXML
    private void showMonthlyReport() {
        showView(
                "/com/kayque/compensa/report/monthly-financial-report-view.fxml",
                monthlyReportButton,
                "Não foi possível abrir o relatório mensal."
        );
    }

    @FXML
    private void showAlertCenter() {
        showView(
                "/com/kayque/compensa/alerts/alert-center-view.fxml",
                alertCenterButton,
                "Não foi possível abrir a central de alertas."
        );
    }

    @FXML
    private void showSavingsGoal() {
        showView(
                "/com/kayque/compensa/goal/savings-goal-view.fxml",
                savingsGoalButton,
                "Não foi possível abrir o objetivo financeiro."
        );
    }

    @FXML
    private void showUserProfile() {
        showView(
                "/com/kayque/compensa/userprofile/user-profile-view.fxml",
                userProfileButton,
                "Não foi possível abrir o perfil do usuário."
        );
    }

    @FXML
    private void showFinancialProfile() {
        showView(
                "/com/kayque/compensa/profile/profile-view.fxml",
                financialProfileButton,
                "Não foi possível abrir o perfil financeiro."
        );
    }

    @FXML
    private void showSettings() {
        showView(
                "/com/kayque/compensa/settings/settings-view.fxml",
                settingsButton,
                "Não foi possível abrir as configurações."
        );
    }

    private void handleNavigationRequest(
            NavigationRequestEvent event
    ) {
        NavigationTarget target = event.target();

        switch (target) {
            case ALERT_CENTER ->
                    showAlertCenter();

            case FINANCIAL_PROFILE ->
                    showFinancialProfile();

            case SAVINGS_GOAL ->
                    showSavingsGoal();

            case HISTORY ->
                    showHistory();

            case INSIGHTS ->
                    showInsights();

            case NONE -> {
                return;
            }
        }

        event.consume();
    }

    private void showView(
            String resource,
            Button activeButton,
            String errorMessage
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainController.class.getResource(resource)
            );

            Parent view = loader.load();

            showContent(view);
            setActiveButton(activeButton);

        } catch (IOException exception) {
            throw new IllegalStateException(
                    errorMessage,
                    exception
            );
        }
    }

    private void showContent(Node view) {
        mainContentScrollPane.setContent(view);
        mainContentScrollPane.setVvalue(0);
        mainContentScrollPane.setHvalue(0);

        refreshAlertBadge();
    }

    private void refreshAlertBadge() {
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

            int unreadTotal = (int) visibleAlerts
                    .stream()
                    .filter(alert ->
                            !smartAlertReadService.isRead(
                                    alert.code()
                            )
                    )
                    .count();

            updateAlertBadge(unreadTotal);

        } catch (RuntimeException exception) {
            updateAlertBadge(0);
        }
    }

    private void updateAlertBadge(int unreadTotal) {
        boolean visible = unreadTotal > 0;

        alertBadgeLabel.setVisible(visible);
        alertBadgeLabel.setManaged(visible);

        if (!visible) {
            alertBadgeLabel.setText("");
            return;
        }

        alertBadgeLabel.setText(
                unreadTotal > 99
                        ? "99+"
                        : String.valueOf(unreadTotal)
        );
    }

    private void setActiveButton(Button activeButton) {
        todayButton.getStyleClass().setAll(
                "nav-button"
        );

        analyzePurchaseButton.getStyleClass().setAll(
                "nav-button"
        );

        wishlistButton.getStyleClass().setAll(
                "nav-button"
        );

        historyButton.getStyleClass().setAll(
                "nav-button"
        );

        insightsButton.getStyleClass().setAll(
                "nav-button"
        );

        monthlyReportButton.getStyleClass().setAll(
                "nav-button"
        );

        alertCenterButton.getStyleClass().setAll(
                "nav-button"
        );

        savingsGoalButton.getStyleClass().setAll(
                "nav-button"
        );

        userProfileButton.getStyleClass().setAll(
                "nav-button"
        );

        financialProfileButton.getStyleClass().setAll(
                "nav-button"
        );

        settingsButton.getStyleClass().setAll(
                "nav-button"
        );

        activeButton.getStyleClass().setAll(
                "nav-button-active"
        );
    }
}