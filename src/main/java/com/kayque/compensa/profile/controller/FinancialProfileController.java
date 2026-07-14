package com.kayque.compensa.profile.controller;

import com.kayque.compensa.profile.model.FinancialProfile;
import com.kayque.compensa.profile.repository.FinancialProfileRepository;
import com.kayque.compensa.profile.repository.SqliteFinancialProfileRepository;
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

    private final FinancialProfileRepository profileRepository =
            new SqliteFinancialProfileRepository();

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
    private void initialize() {
        loadFinancialProfile();
    }

    @FXML
    private void saveFinancialProfile() {
        clearFeedback();

        try {
            FinancialProfile profile = createProfileFromFields();

            profileRepository.save(profile);

            showCalculatedValues(profile);
            showSuccess("Perfil financeiro salvo com sucesso.");

        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());

        } catch (IllegalStateException exception) {
            showError(
                    "Não foi possível acessar o banco de dados."
            );
        }
    }

    private void loadFinancialProfile() {
        try {
            profileRepository.find().ifPresent(profile -> {
                fillFields(profile);
                showCalculatedValues(profile);
            });

        } catch (IllegalStateException exception) {
            showError(
                    "Não foi possível carregar o perfil financeiro."
            );
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

    private void fillFields(FinancialProfile profile) {
        netMonthlyIncomeField.setText(
                profile.netMonthlyIncome().toPlainString()
        );

        monthlyWorkHoursField.setText(
                profile.monthlyWorkHours().toPlainString()
        );

        monthlyAdditionalHoursField.setText(
                profile.monthlyAdditionalHours().toPlainString()
        );
    }

    private void showCalculatedValues(
            FinancialProfile profile
    ) {
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
                    .replace(" ", "");

            if (normalizedValue.contains(",")) {
                normalizedValue = normalizedValue
                        .replace(".", "")
                        .replace(",", ".");
            }

            return new BigDecimal(normalizedValue);

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private void clearFeedback() {
        feedbackLabel.setText("");
        feedbackLabel.getStyleClass().setAll("feedback-label");
    }

    private void showSuccess(String message) {
        feedbackLabel.setText(message);
        feedbackLabel.getStyleClass().setAll(
                "feedback-label",
                "feedback-success"
        );
    }

    private void showError(String message) {
        feedbackLabel.setText(message);
        feedbackLabel.getStyleClass().setAll(
                "feedback-label",
                "feedback-error"
        );
    }
}