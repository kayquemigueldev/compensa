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
import com.kayque.compensa.purchase.model.PurchaseAdviceMessage;
import com.kayque.compensa.purchase.service.PurchaseAdviceMessageService;
import com.kayque.compensa.userprofile.model.RecommendationTone;
import com.kayque.compensa.userprofile.repository.SqliteUserProfileRepository;
import com.kayque.compensa.userprofile.repository.UserProfileRepository;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import com.kayque.compensa.profile.model.MonthlyBudgetSummary;
import com.kayque.compensa.profile.service.MonthlyBudgetService;
import com.kayque.compensa.purchase.model.PurchaseBudgetImpact;
import com.kayque.compensa.purchase.model.PurchaseBudgetImpactStatus;
import com.kayque.compensa.purchase.service.PurchaseBudgetImpactService;
import com.kayque.compensa.profile.model.MonthlyBudgetUsage;
import com.kayque.compensa.profile.service.MonthlyBudgetUsageService;
import com.kayque.compensa.purchase.service.CurrentMonthPurchasedAmountService;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.stream.Collectors;

public class PurchaseAnalysisController {

    private final PurchaseAnalysisService analysisService =
            new PurchaseAnalysisService();

    private final MonthlyBudgetService budgetService =
            new MonthlyBudgetService();

    private final PurchaseBudgetImpactService budgetImpactService =
            new PurchaseBudgetImpactService();

    private final PurchaseAdviceService adviceService =
            new PurchaseAdviceService();

    private final PurchaseAdviceMessageService
            adviceMessageService =
            new PurchaseAdviceMessageService();

    private final UserProfileRepository userProfileRepository =
            new SqliteUserProfileRepository();

    private final FinancialProfileRepository profileRepository =
            new SqliteFinancialProfileRepository();

    private final PurchaseDecisionRepository decisionRepository =
            new SqlitePurchaseDecisionRepository();

    private final CurrentMonthPurchasedAmountService
            currentMonthPurchasedAmountService =
            new CurrentMonthPurchasedAmountService(
                    decisionRepository
            );

    private final MonthlyBudgetUsageService budgetUsageService =
            new MonthlyBudgetUsageService();

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(
                    Locale.of("pt", "BR")
            );

    private PurchaseAnalysis currentAnalysis;
    private PurchaseBudgetImpact currentBudgetImpact;
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
    private Label budgetImpactPercentageLabel;

    @FXML
    private Label budgetImpactDetailLabel;

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
    private Button analysisSubmitButton;

    @FXML
    private Button newAnalysisButton;

    @FXML
    private void initialize() {
        configureFrequencyComboBox();
        configureBooleanComboBox(plannedComboBox);
        configureBooleanComboBox(alternativeComboBox);
        configureBooleanComboBox(urgentComboBox);
        configureMotivationComboBox();

        setDecisionButtonsDisabled(true);
        setNewAnalysisButtonVisible(false);
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

            MonthlyBudgetSummary plannedBudget =
                    budgetService.calculate(profile);

            BigDecimal purchasedThisMonth =
                    currentMonthPurchasedAmountService.calculate();

            MonthlyBudgetUsage currentBudget =
                    budgetUsageService.calculate(
                            plannedBudget,
                            purchasedThisMonth
                    );

            PurchaseBudgetImpact budgetImpact =
                    budgetImpactService.calculate(
                            purchase,
                            currentBudget
                    );

            PurchaseDecisionContext context =
                    createDecisionContext();

            PurchaseAdvice advice =
                    adviceService.evaluate(
                            analysis,
                            context,
                            budgetImpact
                    );

            currentAnalysis = analysis;
            currentBudgetImpact = budgetImpact;
            currentContext = context;
            currentAdvice = advice;

            showAnalysis(analysis);
            showBudgetImpact(budgetImpact);
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
            clearCurrentAnalysis();

            analysisSubmitButton.setDisable(true);
            setNewAnalysisButtonVisible(true);

        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    @FXML
    private void startNewAnalysis() {
        purchaseNameField.clear();
        purchasePriceField.clear();

        frequencyComboBox.setValue(
                PurchaseFrequency.ONCE
        );

        plannedComboBox.setValue(null);
        alternativeComboBox.setValue(null);
        urgentComboBox.setValue(null);
        motivationComboBox.setValue(null);

        clearCurrentAnalysis();
        clearFeedback();
        resetAnalysisResult();

        setDecisionButtonsDisabled(true);
        analysisSubmitButton.setDisable(false);
        setNewAnalysisButtonVisible(false);

        purchaseNameField.requestFocus();
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

    private void showBudgetImpact(
            PurchaseBudgetImpact impact
    ) {
        PurchaseBudgetImpactStatus status =
                impact.status();

        budgetImpactPercentageLabel.setText(
                formatBudgetPercentage(impact)
        );

        budgetImpactDetailLabel.setText(
                formatBudgetImpactDetail(impact)
        );

        String styleClass =
                getBudgetImpactStyle(status);

        budgetImpactPercentageLabel
                .getStyleClass()
                .setAll(
                        "metric-value",
                        styleClass
                );

        budgetImpactDetailLabel
                .getStyleClass()
                .setAll(
                        "budget-impact-detail",
                        styleClass
                );
    }

    private String formatBudgetPercentage(
            PurchaseBudgetImpact impact
    ) {
        return impact.budgetUsagePercentage()
                .map(percentage -> String.format(
                        Locale.of("pt", "BR"),
                        "%.2f%%",
                        percentage
                ))
                .orElse("--");
    }

    private String formatBudgetImpactDetail(
            PurchaseBudgetImpact impact
    ) {
        return switch (impact.status()) {
            case WITHIN_BUDGET ->
                    currencyFormat.format(
                            impact.remainingAfterPurchase()
                    ) + " livres após a compra";

            case EXCEEDS_AVAILABLE_AMOUNT ->
                    "Ultrapassa em " + currencyFormat.format(
                            impact.remainingAfterPurchase().abs()
                    );

            case NO_AVAILABLE_BUDGET ->
                    "Sem dinheiro livre neste mês";

            case BUDGET_IN_DEFICIT ->
                    "O orçamento já está em déficit";
        };
    }

    private String getBudgetImpactStyle(
            PurchaseBudgetImpactStatus status
    ) {
        return switch (status) {
            case WITHIN_BUDGET ->
                    "budget-impact-positive";

            case EXCEEDS_AVAILABLE_AMOUNT,
                 NO_AVAILABLE_BUDGET ->
                    "budget-impact-warning";

            case BUDGET_IN_DEFICIT ->
                    "budget-impact-negative";
        };
    }

    private void showAdvice(PurchaseAdvice advice) {
        PurchaseDecisionStatus status = advice.status();

        RecommendationTone tone =
                loadRecommendationTone();

        PurchaseAdviceMessage message =
                adviceMessageService.create(
                        status,
                        tone
                );

        analysisStatusLabel.setText(message.title());

        analysisDescriptionLabel.setText(
                message.description()
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

    private RecommendationTone loadRecommendationTone() {
        try {
            return userProfileRepository
                    .find()
                    .map(profile ->
                            profile.recommendationTone()
                    )
                    .orElse(RecommendationTone.BALANCED);

        } catch (IllegalStateException exception) {
            return RecommendationTone.BALANCED;
        }
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
                && currentBudgetImpact != null
                && currentContext != null
                && currentAdvice != null;
    }

    private void clearCurrentAnalysis() {
        currentAnalysis = null;
        currentBudgetImpact = null;
        currentContext = null;
        currentAdvice = null;

    }

    private void resetAnalysisResult() {
        analysisStatusLabel.setText(
                "SUA DECISÃO COMEÇA AQUI"
        );

        analysisDescriptionLabel.setText(
                "Informe os dados e responda às perguntas para receber uma análise consciente."
        );

        analysisStatusLabel.getStyleClass().setAll(
                "analysis-status"
        );

        professionalTimeLabel.setText("--");
        realTimeLabel.setText("--");
        yearlyCostLabel.setText("--");

        budgetImpactPercentageLabel.setText("--");
        budgetImpactDetailLabel.setText(
                "Impacto no dinheiro livre"
        );

        budgetImpactPercentageLabel
                .getStyleClass()
                .setAll("metric-value");

        budgetImpactDetailLabel
                .getStyleClass()
                .setAll("budget-impact-detail");

        reflectionTextLabel.setText(
                "Os fatores positivos e pontos de atenção aparecerão aqui."
        );
    }

    private void setNewAnalysisButtonVisible(
            boolean visible
    ) {
        newAnalysisButton.setVisible(visible);
        newAnalysisButton.setManaged(visible);
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