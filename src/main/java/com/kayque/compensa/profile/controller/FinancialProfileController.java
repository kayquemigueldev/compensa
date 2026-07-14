package com.kayque.compensa.profile.controller;

import com.kayque.compensa.profile.model.FinancialProfile;
import com.kayque.compensa.profile.service.WorkValueService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class FinancialProfileController {

    private final WorkValueService workValueService =
            new WorkValueService();

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(
                    Locale.of("pt", "BR")
            );

    @FXML
    private TextField netMonthlyIncomeField;

    @FXML
    private TextField monthlyWorkHoursField;

    @FXML
    private TextField monthlyAdditionalHoursField;

    @FXML
    private Label professionalHourlyValueLabel;

    @FXML
    private Label realHourlyValueLabel;

    @FXML
    private Label feedbackLabel;

    @FXML
    private void calculateHourlyValues() {
        clearFeedback();

        try {
            FinancialProfile profile = createProfileFromFields();

            BigDecimal professionalHourlyValue =
                    workValueService
                            .calculateProfessionalHourlyValue(profile);

            BigDecimal realHourlyValue =
                    workValueService
                            .calculateRealHourlyValue(profile);

            professionalHourlyValueLabel.setText(
                    currencyFormat.format(professionalHourlyValue)
            );

            realHourlyValueLabel.setText(
                    currencyFormat.format(realHourlyValue)
            );

            feedbackLabel.setText(
                    "Valores calculados com sucesso."
            );

            feedbackLabel.getStyleClass().setAll(
                    "feedback-label",
                    "feedback-success"
            );

        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
        }
    }

    private FinancialProfile createProfileFromFields() {
        BigDecimal netMonthlyIncome = parseRequiredValue(
                netMonthlyIncomeField.getText(),
                "Informe uma renda mensal válida."
        );

        BigDecimal monthlyWorkHours = parseRequiredValue(
                monthlyWorkHoursField.getText(),
                "Informe uma quantidade válida de horas trabalhadas."
        );

        BigDecimal monthlyAdditionalHours = parseOptionalValue(
                monthlyAdditionalHoursField.getText()
        );

        return new FinancialProfile(
                netMonthlyIncome,
                monthlyWorkHours,
                monthlyAdditionalHours
        );
    }

    private BigDecimal parseRequiredValue(
            String text,
            String errorMessage
    ) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(errorMessage);
        }

        return parseValue(text, errorMessage);
    }

    private BigDecimal parseOptionalValue(String text) {
        if (text == null || text.isBlank()) {
            return BigDecimal.ZERO;
        }

        return parseValue(
                text,
                "Informe uma quantidade válida de horas adicionais."
        );
    }

    private BigDecimal parseValue(
            String text,
            String errorMessage
    ) {
        try {
            String normalizedValue = text
                    .trim()
                    .replace("R$", "")
                    .replace(".", "")
                    .replace(",", ".");

            return new BigDecimal(normalizedValue);

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private void clearFeedback() {
        feedbackLabel.setText("");
        feedbackLabel.getStyleClass().setAll("feedback-label");
    }

    private void showError(String message) {
        professionalHourlyValueLabel.setText("R$ 0,00");
        realHourlyValueLabel.setText("R$ 0,00");

        feedbackLabel.setText(message);
        feedbackLabel.getStyleClass().setAll(
                "feedback-label",
                "feedback-error"
        );
    }
}