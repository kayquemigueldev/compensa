package com.kayque.compensa;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class MainController {

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
    private Button settingsButton;

    private Node purchaseAnalysisView;

    @FXML
    private void showWishlist() {
        showView(
                "/com/kayque/compensa/wishlist/wishlist-view.fxml",
                wishlistButton,
                "Não foi possível abrir a lista de desejos."
        );
    }

    @FXML
    private void initialize() {
        purchaseAnalysisView = mainBorderPane.getCenter();
    }

    @FXML
    private void showPurchaseAnalysis() {
        mainBorderPane.setCenter(purchaseAnalysisView);
        setActiveButton(analyzePurchaseButton);
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
    private void showHistory() {
        showView(
                "/com/kayque/compensa/history/history-view.fxml",
                historyButton,
                "Não foi possível abrir o histórico."
        );
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
    private void showInsights() {
        showView(
                "/com/kayque/compensa/insights/insights-view.fxml",
                insightsButton,
                "Não foi possível abrir os insights."
        );
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

            mainBorderPane.setCenter(view);
            setActiveButton(activeButton);

        } catch (IOException exception) {
            throw new IllegalStateException(
                    errorMessage,
                    exception
            );
        }
    }

    @FXML
    private void showSettings() {
        showView(
                "/com/kayque/compensa/settings/settings-view.fxml",
                settingsButton,
                "Não foi possível abrir as configurações."
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