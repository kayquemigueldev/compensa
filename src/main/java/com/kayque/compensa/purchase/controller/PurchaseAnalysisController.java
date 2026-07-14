package com.kayque.compensa.purchase.controller;

import com.kayque.compensa.profile.model.FinancialProfile;
import com.kayque.compensa.profile.repository.FinancialProfileRepository;
import com.kayque.compensa.profile.repository.SqliteFinancialProfileRepository;
import com.kayque.compensa.purchase.model.Purchase;
import com.kayque.compensa.purchase.model.PurchaseAnalysis;
import com.kayque.compensa.purchase.model.PurchaseFrequency;
import com.kayque.compensa.purchase.service.PurchaseAnalysisService;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class PurchaseAnalysisController {

    private final PurchaseAnalysisService analysisService =
            new PurchaseAnalysisService();

    private final FinancialProfileRepository profileRepository =
            new SqliteFinancialProfileRepository();

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(
                    Locale.of("pt", "BR")
            );

    @FXML
    private TextField purchaseNameField;

    @FXML
    private TextField purchasePriceField;

    @FXML
    private ComboBox<PurchaseFrequency> frequencyComboBox;

    @FXML
    private Label analysisStatusLabel;

    @FXML
    private Label analysisDescriptionLabel;

    @FXML
    private Label professionalTimeLabel;

    @FXML
    private Label realTimeLabel;

    @FXML
    private Label yearlyCostLabel;

    @FXML
    private Label analysisFeedbackLabel;

    @FXML
    private void initialize() {
        configureFrequencyComboBox();
    }

    @FXML
    private void analyzePurchase() {
        clearFeedback();

        try {
            FinancialProfile profile = profileRepository
                    .find()
                    .orElseThrow(() -> new IllegalStateException(
                            "Cadastre seu perfil financeiro antes de analisar uma compra."
                    ));

            Purchase purchase = new Purchase(
                    purchaseNameField.getText(),
                    parsePrice(purchasePriceField.getText()),
                    frequencyComboBox.getValue()
            );

            PurchaseAnalysis analysis =
                    analysisService.analyze(purchase, profile);

            showAnalysis(analysis);

        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());

        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private void configureFrequencyComboBox() {
        frequencyComboBox.getItems().setAll(
                PurchaseFrequency.values()
        );

        frequencyComboBox.setConverter(
                new StringConverter<>() {
                    @Override
                    public String toString(
                            PurchaseFrequency frequency
                    ) {
                        if (frequency == null) {
                            return "";
                        }

                        return switch (frequency) {
                            case ONCE -> "Apenas uma vez";
                            case MONTHLY -> "Uma vez por mês";
                            case WEEKLY -> "Uma vez por semana";
                            case DAILY -> "Todos os dias";
                        };
                    }

                    @Override
                    public PurchaseFrequency fromString(
                            String value
                    ) {
                        return null;
                    }
                }
        );

        frequencyComboBox.setValue(PurchaseFrequency.ONCE);
    }

    private BigDecimal parsePrice(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Informe o preço da compra."
            );
        }

        try {
            String normalizedValue = text
                    .trim()
                    .replace("R$", "")
                    .replace(" ", "");

            if (normalizedValue.contains(",")) {
                normalizedValue = normalizedValue
                        .replace(".", "")
                        .replace(",", ".");
            }

            return new BigDecimal(normalizedValue);

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Informe um preço válido."
            );
        }
    }

    private void showAnalysis(PurchaseAnalysis analysis) {
        analysisStatusLabel.setText("IMPACTO CALCULADO");

        analysisDescriptionLabel.setText(
                "Veja quanto essa escolha representa em tempo de trabalho e repetição ao longo do ano."
        );

        professionalTimeLabel.setText(
                formatWorkTime(
                        analysis.professionalWorkMinutes()
                )
        );

        realTimeLabel.setText(
                formatWorkTime(
                        analysis.realWorkMinutes()
                )
        );

        yearlyCostLabel.setText(
                currencyFormat.format(
                        analysis.projectedYearlyCost()
                )
        );

        analysisFeedbackLabel.setText(
                "Análise concluída. A decisão continua sendo sua."
        );

        analysisFeedbackLabel.getStyleClass().setAll(
                "feedback-label",
                "feedback-success"
        );
    }

    private String formatWorkTime(long totalMinutes) {
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        if (hours == 0) {
            return minutes + " min";
        }

        if (minutes == 0) {
            return hours + "h";
        }

        return hours + "h " + minutes + "min";
    }

    private void clearFeedback() {
        analysisFeedbackLabel.setText("");
        analysisFeedbackLabel
                .getStyleClass()
                .setAll("feedback-label");
    }

    private void showError(String message) {
        analysisFeedbackLabel.setText(message);

        analysisFeedbackLabel.getStyleClass().setAll(
                "feedback-label",
                "feedback-error"
        );
    }
}