package com.kayque.compensa.goal.controller;

import com.kayque.compensa.goal.model.SavingsGoal;
import com.kayque.compensa.goal.model.SavingsGoalProgress;
import com.kayque.compensa.goal.model.SavingsGoalProgressStatus;
import com.kayque.compensa.goal.repository.SavingsGoalRepository;
import com.kayque.compensa.goal.repository.SqliteSavingsGoalRepository;
import com.kayque.compensa.goal.service.SavingsGoalProgressService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class SavingsGoalController {

    private final SavingsGoalRepository repository =
            new SqliteSavingsGoalRepository();

    private final SavingsGoalProgressService progressService =
            new SavingsGoalProgressService();

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(
                    Locale.of("pt", "BR")
            );

    @FXML
    private TextField goalNameField;

    @FXML
    private TextField targetAmountField;

    @FXML
    private TextField savedAmountField;

    @FXML
    private Label goalTitleLabel;

    @FXML
    private Label savedAmountLabel;

    @FXML
    private Label remainingAmountLabel;

    @FXML
    private Label progressPercentageLabel;

    @FXML
    private Label progressStatusLabel;

    @FXML
    private ProgressBar goalProgressBar;

    @FXML
    private Label goalFeedbackLabel;

    @FXML
    private void initialize() {
        loadGoal();
    }

    @FXML
    private void saveGoal() {
        clearFeedback();

        try {
            SavingsGoal goal = new SavingsGoal(
                    goalNameField.getText(),
                    parseRequiredMoney(
                            targetAmountField.getText(),
                            "Informe o valor necessário para o objetivo."
                    ),
                    parseOptionalMoney(
                            savedAmountField.getText()
                    )
            );

            repository.save(goal);
            fillFields(goal);
            renderGoal(goal);

            showSuccess(
                    "Objetivo salvo com sucesso. Continue avançando uma escolha de cada vez."
            );

        } catch (IllegalArgumentException
                 | NullPointerException exception) {
            showError(exception.getMessage());

        } catch (IllegalStateException exception) {
            showError(
                    "Não foi possível salvar o objetivo."
            );
        }
    }

    private void loadGoal() {
        clearFeedback();

        try {
            repository.find().ifPresentOrElse(
                    goal -> {
                        fillFields(goal);
                        renderGoal(goal);
                    },
                    this::renderEmptyState
            );

        } catch (IllegalStateException exception) {
            renderEmptyState();

            showError(
                    "Não foi possível carregar o objetivo."
            );
        }
    }

    private void fillFields(SavingsGoal goal) {
        goalNameField.setText(goal.name());

        targetAmountField.setText(
                currencyFormat.format(
                        goal.targetAmount()
                )
        );

        savedAmountField.setText(
                currencyFormat.format(
                        goal.savedAmount()
                )
        );
    }

    private void renderGoal(SavingsGoal goal) {
        SavingsGoalProgress progress =
                progressService.calculate(goal);

        goalTitleLabel.setText(goal.name());

        savedAmountLabel.setText(
                currencyFormat.format(
                        progress.savedAmount()
                )
        );

        remainingAmountLabel.setText(
                currencyFormat.format(
                        progress.remainingAmount()
                )
        );

        progressPercentageLabel.setText(
                formatPercentage(
                        progress.percentage()
                )
        );

        goalProgressBar.setProgress(
                progress.percentage()
                        .divide(new BigDecimal("100"))
                        .doubleValue()
        );

        renderStatus(progress.status());
    }

    private void renderStatus(
            SavingsGoalProgressStatus status
    ) {
        String text = switch (status) {
            case NOT_STARTED ->
                    "Seu objetivo está pronto. O próximo passo é registrar o primeiro valor guardado.";

            case IN_PROGRESS ->
                    "Você já começou. Cada contribuição aproxima você da sua conquista.";

            case COMPLETED ->
                    "Objetivo alcançado! Você chegou ao valor planejado.";
        };

        progressStatusLabel.setText(text);

        progressStatusLabel.getStyleClass().setAll(
                "goal-status",
                getStatusStyle(status)
        );
    }

    private String getStatusStyle(
            SavingsGoalProgressStatus status
    ) {
        return switch (status) {
            case NOT_STARTED -> "goal-status-warning";
            case IN_PROGRESS -> "goal-status-progress";
            case COMPLETED -> "goal-status-completed";
        };
    }

    private void renderEmptyState() {
        goalTitleLabel.setText(
                "Defina sua próxima conquista"
        );

        savedAmountLabel.setText("--");
        remainingAmountLabel.setText("--");
        progressPercentageLabel.setText("0%");

        goalProgressBar.setProgress(0);

        progressStatusLabel.setText(
                "Preencha os dados ao lado para começar a acompanhar seu progresso."
        );

        progressStatusLabel.getStyleClass().setAll(
                "goal-status",
                "goal-status-warning"
        );
    }

    private BigDecimal parseRequiredMoney(
            String text,
            String emptyMessage
    ) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    emptyMessage
            );
        }

        return parseMoney(text);
    }

    private BigDecimal parseOptionalMoney(String text) {
        if (text == null || text.isBlank()) {
            return BigDecimal.ZERO;
        }

        return parseMoney(text);
    }

    private BigDecimal parseMoney(String text) {
        try {
            String normalizedValue = text
                    .trim()
                    .replace("R$", "")
                    .replace("\u00A0", "")
                    .replace("\u202F", "")
                    .replace(" ", "");

            if (normalizedValue.contains(",")) {
                normalizedValue = normalizedValue
                        .replace(".", "")
                        .replace(",", ".");
            }

            return new BigDecimal(normalizedValue);

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Informe um valor financeiro válido."
            );
        }
    }

    private String formatPercentage(
            BigDecimal percentage
    ) {
        return percentage
                .stripTrailingZeros()
                .toPlainString()
                .replace(".", ",")
                + "%";
    }

    private void clearFeedback() {
        goalFeedbackLabel.setText("");

        goalFeedbackLabel.getStyleClass().setAll(
                "feedback-label"
        );
    }

    private void showSuccess(String message) {
        goalFeedbackLabel.setText(message);

        goalFeedbackLabel.getStyleClass().setAll(
                "feedback-label",
                "feedback-success"
        );
    }

    private void showError(String message) {
        goalFeedbackLabel.setText(message);

        goalFeedbackLabel.getStyleClass().setAll(
                "feedback-label",
                "feedback-error"
        );
    }
}