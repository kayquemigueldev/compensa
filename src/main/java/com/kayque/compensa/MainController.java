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
    private Button financialProfileButton;

    private Node purchaseAnalysisView;

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
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainController.class.getResource(
                            "/com/kayque/compensa/profile/profile-view.fxml"
                    )
            );

            Parent profileView = loader.load();

            mainBorderPane.setCenter(profileView);
            setActiveButton(financialProfileButton);

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Não foi possível abrir o perfil financeiro.",
                    exception
            );
        }
    }

    private void setActiveButton(Button activeButton) {
        analyzePurchaseButton.getStyleClass().setAll(
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