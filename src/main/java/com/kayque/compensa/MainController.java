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

    private void setActiveButton(Button activeButton) {
        analyzePurchaseButton.getStyleClass().setAll(
                "nav-button"
        );

        wishlistButton.getStyleClass().setAll(
                "nav-button"
        );

        historyButton.getStyleClass().setAll(
                "nav-button"
        );

        financialProfileButton.getStyleClass().setAll(
                "nav-button"
        );

        activeButton.getStyleClass().setAll(
                "nav-button-active"
        );
    }
}