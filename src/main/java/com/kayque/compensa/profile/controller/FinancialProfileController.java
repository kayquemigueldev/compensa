package com.kayque.compensa.profile.controller;

import com.kayque.compensa.profile.model.FinancialProfile;
import com.kayque.compensa.profile.model.MonthlyBudgetStatus;
import com.kayque.compensa.profile.model.MonthlyBudgetSummary;
import com.kayque.compensa.profile.repository.FinancialProfileRepository;
import com.kayque.compensa.profile.repository.SqliteFinancialProfileRepository;
import com.kayque.compensa.profile.service.MonthlyBudgetService;
import com.kayque.compensa.profile.service.WorkValueService;
import com.kayque.compensa.profile.model.MonthlyWorkHoursCalculation;
import com.kayque.compensa.profile.service.MonthlyWorkHoursCalculatorService;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
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

    private final MonthlyWorkHoursCalculatorService
            workHoursCalculatorService =
            new MonthlyWorkHoursCalculatorService();

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

    @FXML
    private void openWorkHoursCalculator() {
        clearFeedback();

        Dialog<Void> dialog = new Dialog<>();

        dialog.setTitle("Calcular horas mensais");

        dialog.initOwner(
                monthlyWorkHoursField
                        .getScene()
                        .getWindow()
        );

        ButtonType useValuesButtonType =
                new ButtonType(
                        "Usar estes valores no perfil",
                        javafx.scene.control.ButtonBar
                                .ButtonData.OK_DONE
                );

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        useValuesButtonType,
                        ButtonType.CANCEL
                );

        TextField professionalHoursPerDayField =
                new TextField("8");

        professionalHoursPerDayField.setPromptText(
                "Ex.: 8"
        );

        professionalHoursPerDayField
                .getStyleClass()
                .add("form-field");

        TextField workDaysPerWeekField =
                new TextField("5");

        workDaysPerWeekField.setPromptText(
                "Ex.: 5"
        );

        workDaysPerWeekField
                .getStyleClass()
                .add("form-field");

        TextField additionalHoursPerDayField =
                new TextField("0");

        additionalHoursPerDayField.setPromptText(
                "Ex.: 1,5"
        );

        additionalHoursPerDayField
                .getStyleClass()
                .add("form-field");

        Label professionalMonthlyResultLabel =
                createCalculatorResultLabel();

        Label additionalMonthlyResultLabel =
                createCalculatorResultLabel();

        Label committedMonthlyResultLabel =
                createCalculatorResultLabel();

        Label calculatorErrorLabel = new Label();

        calculatorErrorLabel.setWrapText(true);

        calculatorErrorLabel.getStyleClass().add(
                "work-hours-calculator-error"
        );

        GridPane fieldsGrid = new GridPane();

        fieldsGrid.setHgap(14);
        fieldsGrid.setVgap(9);

        fieldsGrid.add(
                createCalculatorFieldLabel(
                        "Horas de trabalho por dia"
                ),
                0,
                0
        );

        fieldsGrid.add(
                professionalHoursPerDayField,
                0,
                1
        );

        fieldsGrid.add(
                createCalculatorFieldLabel(
                        "Dias trabalhados por semana"
                ),
                1,
                0
        );

        fieldsGrid.add(
                workDaysPerWeekField,
                1,
                1
        );

        fieldsGrid.add(
                createCalculatorFieldLabel(
                        "Tempo adicional por dia"
                ),
                0,
                2,
                2,
                1
        );

        fieldsGrid.add(
                additionalHoursPerDayField,
                0,
                3,
                2,
                1
        );

        professionalHoursPerDayField.setMaxWidth(
                Double.MAX_VALUE
        );

        workDaysPerWeekField.setMaxWidth(
                Double.MAX_VALUE
        );

        additionalHoursPerDayField.setMaxWidth(
                Double.MAX_VALUE
        );

        GridPane.setHgrow(
                professionalHoursPerDayField,
                javafx.scene.layout.Priority.ALWAYS
        );

        GridPane.setHgrow(
                workDaysPerWeekField,
                javafx.scene.layout.Priority.ALWAYS
        );

        VBox results = new VBox(
                8,
                createCalculatorResultTitle(
                        "Horas profissionais no mês"
                ),
                professionalMonthlyResultLabel,

                createCalculatorResultTitle(
                        "Horas adicionais no mês"
                ),
                additionalMonthlyResultLabel,

                createCalculatorResultTitle(
                        "Tempo total comprometido"
                ),
                committedMonthlyResultLabel
        );

        results.getStyleClass().add(
                "work-hours-calculator-results"
        );

        Label descriptionLabel = new Label(
                "Informe sua rotina semanal. O Compensa? utilizará a média de 52 semanas divididas pelos 12 meses do ano."
        );

        descriptionLabel.setWrapText(true);

        descriptionLabel.getStyleClass().add(
                "work-hours-calculator-description"
        );

        VBox content = new VBox(
                16,
                descriptionLabel,
                fieldsGrid,
                results,
                calculatorErrorLabel
        );

        content.setPadding(
                new Insets(4, 0, 4, 0)
        );

        content.getStyleClass().add(
                "work-hours-calculator-content"
        );

        DialogPane dialogPane = dialog.getDialogPane();

        dialogPane.setContent(content);
        dialogPane.setPrefWidth(560);

        dialogPane.getStyleClass().add(
                "work-hours-calculator-dialog"
        );

        dialogPane.getStylesheets().add(
                FinancialProfileController.class
                        .getResource(
                                "/com/kayque/compensa/styles.css"
                        )
                        .toExternalForm()
        );

        Node useValuesButton =
                dialogPane.lookupButton(
                        useValuesButtonType
                );

        useValuesButton.getStyleClass().add(
                "work-hours-calculator-use-button"
        );

        Runnable updatePreview = () ->
                updateWorkHoursCalculatorPreview(
                        professionalHoursPerDayField,
                        workDaysPerWeekField,
                        additionalHoursPerDayField,
                        professionalMonthlyResultLabel,
                        additionalMonthlyResultLabel,
                        committedMonthlyResultLabel
                );

        professionalHoursPerDayField
                .textProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                updatePreview.run()
                );

        workDaysPerWeekField
                .textProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                updatePreview.run()
                );

        additionalHoursPerDayField
                .textProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                updatePreview.run()
                );

        useValuesButton.addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    try {
                        MonthlyWorkHoursCalculation calculation =
                                calculateMonthlyWorkHours(
                                        professionalHoursPerDayField,
                                        workDaysPerWeekField,
                                        additionalHoursPerDayField
                                );

                        monthlyWorkHoursField.setText(
                                formatDecimalHours(
                                        calculation
                                                .monthlyProfessionalHours()
                                )
                        );

                        monthlyAdditionalHoursField.setText(
                                formatDecimalHours(
                                        calculation
                                                .monthlyAdditionalHours()
                                )
                        );

                        showSuccess(
                                "Horas calculadas e adicionadas ao perfil. Revise os valores e salve o perfil financeiro."
                        );

                    } catch (IllegalArgumentException exception) {
                        calculatorErrorLabel.setText(
                                exception.getMessage()
                        );

                        event.consume();
                    }
                }
        );

        updatePreview.run();
        dialog.showAndWait();
    }

    private void updateWorkHoursCalculatorPreview(
            TextField professionalHoursPerDayField,
            TextField workDaysPerWeekField,
            TextField additionalHoursPerDayField,
            Label professionalMonthlyResultLabel,
            Label additionalMonthlyResultLabel,
            Label committedMonthlyResultLabel
    ) {
        try {
            MonthlyWorkHoursCalculation calculation =
                    calculateMonthlyWorkHours(
                            professionalHoursPerDayField,
                            workDaysPerWeekField,
                            additionalHoursPerDayField
                    );

            professionalMonthlyResultLabel.setText(
                    formatHoursResult(
                            calculation.monthlyProfessionalHours()
                    )
            );

            additionalMonthlyResultLabel.setText(
                    formatHoursResult(
                            calculation.monthlyAdditionalHours()
                    )
            );

            committedMonthlyResultLabel.setText(
                    formatHoursResult(
                            calculation.monthlyCommittedHours()
                    )
            );

        } catch (IllegalArgumentException exception) {
            professionalMonthlyResultLabel.setText("--");
            additionalMonthlyResultLabel.setText("--");
            committedMonthlyResultLabel.setText("--");
        }
    }

    private MonthlyWorkHoursCalculation calculateMonthlyWorkHours(
            TextField professionalHoursPerDayField,
            TextField workDaysPerWeekField,
            TextField additionalHoursPerDayField
    ) {
        BigDecimal professionalHoursPerDay =
                parseRequiredValue(
                        professionalHoursPerDayField.getText(),
                        "Informe as horas trabalhadas por dia."
                );

        int workDaysPerWeek = parseWorkDays(
                workDaysPerWeekField.getText()
        );

        BigDecimal additionalHoursPerDay =
                parseOptionalValue(
                        additionalHoursPerDayField.getText(),
                        "Informe um tempo adicional diário válido."
                );

        return workHoursCalculatorService.calculate(
                professionalHoursPerDay,
                workDaysPerWeek,
                additionalHoursPerDay
        );
    }

    private int parseWorkDays(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Informe os dias trabalhados por semana."
            );
        }

        try {
            return Integer.parseInt(text.trim());

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Os dias trabalhados por semana devem ser um número inteiro."
            );
        }
    }

    private Label createCalculatorFieldLabel(
            String text
    ) {
        Label label = new Label(text);

        label.getStyleClass().add(
                "work-hours-calculator-field-label"
        );

        return label;
    }

    private Label createCalculatorResultTitle(
            String text
    ) {
        Label label = new Label(text);

        label.getStyleClass().add(
                "work-hours-calculator-result-title"
        );

        return label;
    }

    private Label createCalculatorResultLabel() {
        Label label = new Label("--");

        label.getStyleClass().add(
                "work-hours-calculator-result-value"
        );

        return label;
    }

    private String formatHoursResult(
            BigDecimal hours
    ) {
        return formatDecimalHours(hours) + " horas";
    }

    private String formatDecimalHours(
            BigDecimal hours
    ) {
        return hours
                .stripTrailingZeros()
                .toPlainString();
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