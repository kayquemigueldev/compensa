package com.kayque.compensa.goal.controller;

import com.kayque.compensa.goal.model.SavingsGoal;
import com.kayque.compensa.goal.model.SavingsGoalContribution;
import com.kayque.compensa.goal.model.SavingsGoalProgress;
import com.kayque.compensa.goal.model.SavingsGoalProgressStatus;
import com.kayque.compensa.goal.repository.SavingsGoalContributionRepository;
import com.kayque.compensa.goal.repository.SavingsGoalRepository;
import com.kayque.compensa.goal.repository.SqliteSavingsGoalContributionRepository;
import com.kayque.compensa.goal.repository.SqliteSavingsGoalRepository;
import com.kayque.compensa.goal.service.SavingsGoalProgressService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class SavingsGoalController {

    private static final int HISTORY_LIMIT = 5;

    private final SavingsGoalRepository repository =
            new SqliteSavingsGoalRepository();

    private final SavingsGoalContributionRepository
            contributionRepository =
            new SqliteSavingsGoalContributionRepository();

    private final SavingsGoalProgressService progressService =
            new SavingsGoalProgressService();

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(
                    Locale.of("pt", "BR")
            );

    private final DateTimeFormatter dateFormat =
            DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy 'às' HH:mm"
            );

    private SavingsGoal currentGoal;

    @FXML
    private TextField goalNameField;

    @FXML
    private TextField targetAmountField;

    @FXML
    private TextField savedAmountField;

    @FXML
    private TextField contributionAmountField;

    @FXML
    private Button addContributionButton;

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
    private VBox contributionHistoryContainer;

    @FXML
    private Label contributionEmptyLabel;

    @FXML
    private void initialize() {
        loadGoal();
        loadContributions();
    }

    @FXML
    private void saveGoal() {
        clearFeedback();

        try {
            BigDecimal savedAmount =
                    currentGoal == null
                            ? parseOptionalMoney(
                            savedAmountField.getText()
                    )
                            : currentGoal.savedAmount();

            SavingsGoal goal = new SavingsGoal(
                    goalNameField.getText(),

                    parseRequiredMoney(
                            targetAmountField.getText(),
                            "Informe o valor necessário para o objetivo."
                    ),

                    savedAmount
            );

            repository.save(goal);
            currentGoal = goal;

            fillFields(goal);
            renderGoal(goal);

            showSuccess(
                    "Objetivo salvo com sucesso."
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

    @FXML
    private void addContribution() {
        clearFeedback();

        try {
            if (currentGoal == null) {
                throw new IllegalArgumentException(
                        "Crie e salve um objetivo antes de adicionar uma contribuição."
                );
            }

            BigDecimal amount = parseRequiredMoney(
                    contributionAmountField.getText(),
                    "Informe o valor da contribuição."
            );

            contributionRepository.add(amount);

            contributionAmountField.clear();

            reloadGoal();
            loadContributions();

            showSuccess(
                    "Contribuição registrada com sucesso."
            );

        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());

        } catch (IllegalStateException exception) {
            showError(
                    "Não foi possível registrar a contribuição."
            );
        }
    }

    private void loadGoal() {
        clearFeedback();

        try {
            repository.find().ifPresentOrElse(
                    goal -> {
                        currentGoal = goal;
                        fillFields(goal);
                        renderGoal(goal);
                        setContributionFormEnabled(true);
                    },
                    () -> {
                        currentGoal = null;
                        renderEmptyState();
                        setContributionFormEnabled(false);
                    }
            );

        } catch (IllegalStateException exception) {
            currentGoal = null;
            renderEmptyState();
            setContributionFormEnabled(false);

            showError(
                    "Não foi possível carregar o objetivo."
            );
        }
    }

    private void reloadGoal() {
        currentGoal = repository.find()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "O objetivo não foi encontrado."
                        )
                );

        fillFields(currentGoal);
        renderGoal(currentGoal);
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

        /*
         * Depois que o objetivo existe, o saldo só pode ser
         * alterado por contribuições registradas.
         */
        savedAmountField.setDisable(true);
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
                    "Seu objetivo está pronto. Registre sua primeira contribuição.";

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

    private void loadContributions() {
        try {
            List<SavingsGoalContribution> contributions =
                    contributionRepository.findAll();

            renderContributionHistory(contributions);

        } catch (IllegalStateException exception) {
            contributionHistoryContainer
                    .getChildren()
                    .clear();

            contributionEmptyLabel.setText(
                    "Não foi possível carregar as contribuições."
            );

            contributionEmptyLabel.setVisible(true);
            contributionEmptyLabel.setManaged(true);
        }
    }

    private void renderContributionHistory(
            List<SavingsGoalContribution> contributions
    ) {
        contributionHistoryContainer
                .getChildren()
                .clear();

        boolean empty = contributions.isEmpty();

        contributionEmptyLabel.setVisible(empty);
        contributionEmptyLabel.setManaged(empty);

        if (empty) {
            return;
        }

        contributions.stream()
                .limit(HISTORY_LIMIT)
                .map(this::createContributionRow)
                .forEach(row ->
                        contributionHistoryContainer
                                .getChildren()
                                .add(row)
                );
    }

    private HBox createContributionRow(
            SavingsGoalContribution contribution
    ) {
        Label dateLabel = new Label(
                contribution.contributedAt()
                        .format(dateFormat)
        );

        dateLabel.getStyleClass().add(
                "goal-contribution-date"
        );

        Label descriptionLabel = new Label(
                "Valor adicionado ao objetivo"
        );

        descriptionLabel.getStyleClass().add(
                "goal-contribution-description"
        );

        VBox information = new VBox(
                3,
                dateLabel,
                descriptionLabel
        );

        Label amountLabel = new Label(
                "+ " + currencyFormat.format(
                        contribution.amount()
                )
        );

        amountLabel.getStyleClass().add(
                "goal-contribution-value"
        );

        Region spacer = new Region();
        HBox.setHgrow(
                spacer,
                javafx.scene.layout.Priority.ALWAYS
        );

        HBox row = new HBox(
                12,
                information,
                spacer,
                amountLabel
        );

        row.setAlignment(
                javafx.geometry.Pos.CENTER_LEFT
        );

        row.getStyleClass().add(
                "goal-contribution-row"
        );

        return row;
    }

    private void renderEmptyState() {
        goalTitleLabel.setText(
                "Defina sua próxima conquista"
        );

        savedAmountLabel.setText("--");
        remainingAmountLabel.setText("--");
        progressPercentageLabel.setText("0%");

        goalProgressBar.setProgress(0);
        savedAmountField.setDisable(false);

        progressStatusLabel.setText(
                "Preencha os dados ao lado para começar a acompanhar seu progresso."
        );

        progressStatusLabel.getStyleClass().setAll(
                "goal-status",
                "goal-status-warning"
        );
    }

    private void setContributionFormEnabled(
            boolean enabled
    ) {
        contributionAmountField.setDisable(!enabled);
        addContributionButton.setDisable(!enabled);
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