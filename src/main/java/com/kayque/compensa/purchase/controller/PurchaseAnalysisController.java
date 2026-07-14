package com.kayque.compensa.purchase.controller;

import com.kayque.compensa.profile.model.FinancialProfile;
import com.kayque.compensa.profile.repository.FinancialProfileRepository;
import com.kayque.compensa.profile.repository.SqliteFinancialProfileRepository;
import com.kayque.compensa.purchase.model.Purchase;
import com.kayque.compensa.purchase.model.PurchaseAdvice;
import com.kayque.compensa.purchase.model.PurchaseAnalysis;
import com.kayque.compensa.purchase.model.PurchaseDecision;
import com.kayque.compensa.purchase.model.PurchaseDecisionContext;
import com.kayque.compensa.purchase.model.PurchaseDecisionOutcome;
import com.kayque.compensa.purchase.model.PurchaseDecisionStatus;
import com.kayque.compensa.purchase.model.PurchaseFrequency;
import com.kayque.compensa.purchase.model.PurchaseMotivation;
import com.kayque.compensa.purchase.repository.PurchaseDecisionRepository;
import com.kayque.compensa.purchase.repository.SqlitePurchaseDecisionRepository;
import com.kayque.compensa.purchase.service.PurchaseAdviceService;
import com.kayque.compensa.purchase.service.PurchaseAnalysisService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.stream.Collectors;

public class PurchaseAnalysisController {

    private final PurchaseAnalysisService analysisService =
            new PurchaseAnalysisService();

    private final PurchaseAdviceService adviceService =
            new PurchaseAdviceService();

    private final FinancialProfileRepository profileRepository =
            new SqliteFinancialProfileRepository();

    private final PurchaseDecisionRepository decisionRepository =
            new SqlitePurchaseDecisionRepository();

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(
                    Locale.of("pt", "BR")
            );

    private PurchaseAnalysis currentAnalysis;
    private PurchaseDecisionContext currentContext;
    private PurchaseAdvice currentAdvice;

    @FXML
    private TextField purchaseNameField;

    @FXML
    private TextField purchasePriceField;

    @FXML
    private ComboBox<PurchaseFrequency> frequencyComboBox;

    @FXML
    private ComboBox<Boolean> plannedComboBox;

    @FXML
    private ComboBox<Boolean> alternativeComboBox;

    @FXML
    private ComboBox<Boolean> urgentComboBox;

    @FXML
    private ComboBox<PurchaseMotivation> motivationComboBox;

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
    private Label reflectionTextLabel;

    @FXML
    private Label analysisFeedbackLabel;

    @FXML
    private Button purchaseDecisionButton;

    @FXML
    private Button waitDecisionButton;

    @FXML
    private Button declineDecisionButton;

    @FXML
    private void initialize() {
        configureFrequencyComboBox();
        configureBooleanComboBox(plannedComboBox);
        configureBooleanComboBox(alternativeComboBox);
        configureBooleanComboBox(urgentComboBox);
        configureMotivationComboBox();

        setDecisionButtonsDisabled(true);
    }

    @FXML
    private void analyzePurchase() {
        clearFeedback();
        clearCurrentAnalysis();
        setDecisionButtonsDisabled(true);

        try {
            FinancialProfile profile = profileRepository
                    .find()
                    .orElseThrow(() -> new IllegalStateException(
                            "Cadastre seu perfil financeiro antes de analisar uma compra."
                    ));

            Purchase purchase = createPurchase();

            PurchaseAnalysis analysis =
                    analysisService.analyze(purchase, profile);

            PurchaseDecisionContext context =
                    createDecisionContext();

            PurchaseAdvice advice =
                    adviceService.evaluate(analysis, context);

            currentAnalysis = analysis;
            currentContext = context;
            currentAdvice = advice;

            showAnalysis(analysis);
            showAdvice(advice);
            setDecisionButtonsDisabled(false);

        } catch (IllegalArgumentException |
                 IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    @FXML
    private void choosePurchase() {
        saveDecision(PurchaseDecisionOutcome.PURCHASED);
    }

    @FXML
    private void chooseWaiting() {
        saveDecision(PurchaseDecisionOutcome.WAITING);
    }

    @FXML
    private void chooseDeclined() {
        saveDecision(PurchaseDecisionOutcome.DECLINED);
    }

    private void saveDecision(
            PurchaseDecisionOutcome outcome
    ) {
        if (!hasCurrentAnalysis()) {
            showError(
                    "Faça uma análise antes de registrar sua decisão."
            );
            return;
        }

        try {
            PurchaseDecision decision = new PurchaseDecision(
                    currentAnalysis,
                    currentContext,
                    currentAdvice,
                    outcome
            );

            long generatedId =
                    decisionRepository.save(decision);

            showDecisionSaved(outcome, generatedId);
            setDecisionButtonsDisabled(true);

        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private Purchase createPurchase() {
        return new Purchase(
                purchaseNameField.getText(),
                parsePrice(purchasePriceField.getText()),
                requireSelection(
                        frequencyComboBox.getValue(),
                        "Informe a frequência da compra."
                )
        );
    }

    private PurchaseDecisionContext createDecisionContext() {
        return new PurchaseDecisionContext(
                requireSelection(
                        plannedComboBox.getValue(),
                        "Informe se a compra estava planejada."
                ),
                requireSelection(
                        alternativeComboBox.getValue(),
                        "Informe se existe uma alternativa."
                ),
                requireSelection(
                        urgentComboBox.getValue(),
                        "Informe se a compra é urgente."
                ),
                requireSelection(
                        motivationComboBox.getValue(),
                        "Informe a principal motivação da compra."
                )
        );
    }

    private void showAnalysis(PurchaseAnalysis analysis) {
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
    }

    private void showAdvice(PurchaseAdvice advice) {
        PurchaseDecisionStatus status = advice.status();

        analysisStatusLabel.setText(
                getStatusTitle(status)
        );

        analysisDescriptionLabel.setText(
                getStatusDescription(status)
        );

        analysisStatusLabel.getStyleClass().setAll(
                "analysis-status",
                getStatusStyleClass(status)
        );

        String reasons = advice.reasons()
                .stream()
                .map(reason -> "• " + reason)
                .collect(Collectors.joining("\n"));

        reflectionTextLabel.setText(reasons);

        showSuccess("Análise concluída.");
    }

    private void showDecisionSaved(
            PurchaseDecisionOutcome outcome,
            long generatedId
    ) {
        String decisionText = switch (outcome) {
            case PURCHASED -> "Você decidiu comprar.";
            case WAITING -> "Você decidiu esperar.";
            case DECLINED -> "Você decidiu não comprar.";
        };

        showSuccess(
                decisionText
                        + " Decisão registrada com o número "
                        + generatedId
                        + "."
        );
    }

    private String getStatusTitle(
            PurchaseDecisionStatus status
    ) {
        return switch (status) {
            case MAKES_SENSE -> "FAZ SENTIDO";
            case THINK_AGAIN -> "PENSE MAIS UM POUCO";
            case PROBABLY_NOT_WORTH_IT ->
                    "PROVAVELMENTE NÃO COMPENSA";
        };
    }

    private String getStatusDescription(
            PurchaseDecisionStatus status
    ) {
        return switch (status) {
            case MAKES_SENSE ->
                    "A compra apresenta fatores coerentes com o contexto informado.";

            case THINK_AGAIN ->
                    "A compra é possível, mas existem pontos que merecem reflexão.";

            case PROBABLY_NOT_WORTH_IT ->
                    "A decisão reúne vários sinais de impulso ou baixo benefício imediato.";
        };
    }

    private String getStatusStyleClass(
            PurchaseDecisionStatus status
    ) {
        return switch (status) {
            case MAKES_SENSE -> "status-positive";
            case THINK_AGAIN -> "status-warning";
            case PROBABLY_NOT_WORTH_IT -> "status-negative";
        };
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

    private void configureBooleanComboBox(
            ComboBox<Boolean> comboBox
    ) {
        comboBox.getItems().setAll(true, false);

        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Boolean value) {
                if (value == null) {
                    return "";
                }

                return value ? "Sim" : "Não";
            }

            @Override
            public Boolean fromString(String value) {
                return null;
            }
        });
    }

    private void configureMotivationComboBox() {
        motivationComboBox.getItems().setAll(
                PurchaseMotivation.values()
        );

        motivationComboBox.setConverter(
                new StringConverter<>() {
                    @Override
                    public String toString(
                            PurchaseMotivation motivation
                    ) {
                        if (motivation == null) {
                            return "";
                        }

                        return switch (motivation) {
                            case NEED -> "Necessidade";
                            case DESIRE -> "Desejo";
                            case IMPULSE -> "Impulso";
                        };
                    }

                    @Override
                    public PurchaseMotivation fromString(
                            String value
                    ) {
                        return null;
                    }
                }
        );
    }

    private <T> T requireSelection(
            T value,
            String message
    ) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }

        return value;
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

    private boolean hasCurrentAnalysis() {
        return currentAnalysis != null
                && currentContext != null
                && currentAdvice != null;
    }

    private void clearCurrentAnalysis() {
        currentAnalysis = null;
        currentContext = null;
        currentAdvice = null;
    }

    private void setDecisionButtonsDisabled(
            boolean disabled
    ) {
        purchaseDecisionButton.setDisable(disabled);
        waitDecisionButton.setDisable(disabled);
        declineDecisionButton.setDisable(disabled);
    }

    private void clearFeedback() {
        analysisFeedbackLabel.setText("");
        analysisFeedbackLabel
                .getStyleClass()
                .setAll("feedback-label");
    }

    private void showSuccess(String message) {
        analysisFeedbackLabel.setText(message);

        analysisFeedbackLabel.getStyleClass().setAll(
                "feedback-label",
                "feedback-success"
        );
    }

    private void showError(String message) {
        analysisFeedbackLabel.setText(message);

        analysisFeedbackLabel.getStyleClass().setAll(
                "feedback-label",
                "feedback-error"
        );
    }
}