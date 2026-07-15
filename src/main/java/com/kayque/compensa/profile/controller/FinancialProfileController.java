package com.kayque.compensa.profile.controller;

import com.kayque.compensa.profile.model.FinancialProfile;
import com.kayque.compensa.profile.model.MonthlyBudgetStatus;
import com.kayque.compensa.profile.model.MonthlyBudgetSummary;
import com.kayque.compensa.profile.repository.FinancialProfileRepository;
import com.kayque.compensa.profile.repository.SqliteFinancialProfileRepository;
import com.kayque.compensa.profile.service.MonthlyBudgetService;
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

    private final MonthlyBudgetService budgetService =
            new MonthlyBudgetService();

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
    private TextField essentialExpensesField;

    @FXML
    private TextField monthlySavingsGoalField;

    @FXML
    private Label professionalHourlyValueLabel;

    @FXML
    private Label realHourlyValueLabel;

    @FXML
    private Label availableMonthlyAmountLabel;

    @FXML
    private Label budgetStatusLabel;

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
            FinancialProfile profile =
                    createProfileFromFields();

            profileRepository.save(profile);

            showCalculatedValues(profile);
            showSuccess(
                    "Perfil financeiro salvo com sucesso."
            );

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
        BigDecimal netMonthlyIncome =
                parseRequiredValue(
                        netMonthlyIncomeField.getText(),
                        "Informe uma renda mensal válida."
                );

        BigDecimal monthlyWorkHours =
                parseRequiredValue(
                        monthlyWorkHoursField.getText(),
                        "Informe uma quantidade válida de horas trabalhadas."
                );

        BigDecimal monthlyAdditionalHours =
                parseOptionalValue(
                        monthlyAdditionalHoursField.getText(),
                        "Informe uma quantidade válida de horas adicionais."
                );

        BigDecimal essentialExpenses =
                parseOptionalValue(
                        essentialExpensesField.getText(),
                        "Informe um valor válido para as despesas essenciais."
                );

        BigDecimal monthlySavingsGoal =
                parseOptionalValue(
                        monthlySavingsGoalField.getText(),
                        "Informe um valor válido para a meta de economia."
                );

        return new FinancialProfile(
                netMonthlyIncome,
                monthlyWorkHours,
                monthlyAdditionalHours,
                essentialExpenses,
                monthlySavingsGoal
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

        essentialExpensesField.setText(
                profile.essentialExpenses().toPlainString()
        );

        monthlySavingsGoalField.setText(
                profile.monthlySavingsGoal().toPlainString()
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

        MonthlyBudgetSummary budget =
                budgetService.calculate(profile);

        professionalHourlyValueLabel.setText(
                currencyFormat.format(
                        professionalHourlyValue
                )
        );

        realHourlyValueLabel.setText(
                currencyFormat.format(realHourlyValue)
        );

        showBudget(budget);
    }

    private void showBudget(
            MonthlyBudgetSummary budget
    ) {
        String statusStyle =
                getBudgetStatusStyle(budget.status());

        availableMonthlyAmountLabel.setText(
                currencyFormat.format(
                        budget.availableAmount()
                )
        );

        availableMonthlyAmountLabel
                .getStyleClass()
                .setAll(
                        "budget-value",
                        statusStyle
                );

        budgetStatusLabel.setText(
                formatBudgetStatus(budget.status())
        );

        budgetStatusLabel
                .getStyleClass()
                .setAll(
                        "budget-status",
                        statusStyle
                );
    }

    private String formatBudgetStatus(
            MonthlyBudgetStatus status
    ) {
        return switch (status) {
            case AVAILABLE ->
                    "Disponível para escolhas";
            case BALANCED ->
                    "Orçamento no limite";
            case DEFICIT ->
                    "Orçamento em déficit";
        };
    }

    private String getBudgetStatusStyle(
            MonthlyBudgetStatus status
    ) {
        return switch (status) {
            case AVAILABLE -> "budget-available";
            case BALANCED -> "budget-balanced";
            case DEFICIT -> "budget-deficit";
        };
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

    private BigDecimal parseOptionalValue(
            String text,
            String errorMessage
    ) {
        if (text == null || text.isBlank()) {
            return BigDecimal.ZERO;
        }

        return parseValue(text, errorMessage);
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
        feedbackLabel.getStyleClass().setAll(
                "feedback-label"
        );
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