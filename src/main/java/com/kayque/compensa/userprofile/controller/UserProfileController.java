package com.kayque.compensa.userprofile.controller;

import com.kayque.compensa.userprofile.model.RecommendationTone;
import com.kayque.compensa.userprofile.model.UserGoal;
import com.kayque.compensa.userprofile.model.UserProfile;
import com.kayque.compensa.userprofile.repository.SqliteUserProfileRepository;
import com.kayque.compensa.userprofile.repository.UserProfileRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class UserProfileController {

    @FXML
    private TextField displayNameField;

    @FXML
    private ComboBox<UserGoal> mainGoalComboBox;

    @FXML
    private ComboBox<RecommendationTone>
            recommendationToneComboBox;

    @FXML
    private TextField currentDreamField;

    @FXML
    private TextField currentDreamTargetAmountField;

    @FXML
    private Button saveProfileButton;

    @FXML
    private Label profileFeedbackLabel;

    private final UserProfileRepository repository =
            new SqliteUserProfileRepository();

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(
                    Locale.of("pt", "BR")
            );

    @FXML
    private void initialize() {
        configureGoalComboBox();
        configureToneComboBox();
        loadProfile();
    }

    @FXML
    private void saveProfile() {
        clearFeedback();

        try {
            UserProfile profile = new UserProfile(
                    displayNameField.getText(),
                    mainGoalComboBox.getValue(),
                    recommendationToneComboBox.getValue(),
                    currentDreamField.getText(),
                    parseOptionalMoney(
                            currentDreamTargetAmountField.getText()
                    )
            );

            repository.save(profile);
            fillFields(profile);

            showSuccess(
                    "Perfil salvo com sucesso. Suas preferências já estão seguras neste dispositivo."
            );

        } catch (IllegalArgumentException
                 | NullPointerException exception) {
            showError(exception.getMessage());

        } catch (IllegalStateException exception) {
            showError(
                    "Não foi possível salvar seu perfil."
            );
        }
    }

    private void loadProfile() {
        try {
            repository.find().ifPresent(this::fillFields);

        } catch (IllegalStateException exception) {
            showError(
                    "Não foi possível carregar seu perfil."
            );
        }
    }

    private void fillFields(UserProfile profile) {
        displayNameField.setText(profile.displayName());
        mainGoalComboBox.setValue(profile.mainGoal());

        recommendationToneComboBox.setValue(
                profile.recommendationTone()
        );

        currentDreamField.setText(profile.currentDream());
        if (profile.hasCurrentDreamTargetAmount()) {
            currentDreamTargetAmountField.setText(
                    currencyFormat.format(
                            profile.currentDreamTargetAmount()
                    )
            );
        } else {
            currentDreamTargetAmountField.clear();
        }
    }

    private void configureGoalComboBox() {
        mainGoalComboBox.getItems().setAll(
                UserGoal.values()
        );

        mainGoalComboBox.setConverter(
                createConverter(this::formatGoal)
        );
    }

    private void configureToneComboBox() {
        recommendationToneComboBox.getItems().setAll(
                RecommendationTone.values()
        );

        recommendationToneComboBox.setConverter(
                createConverter(this::formatTone)
        );
    }

    private String formatGoal(UserGoal goal) {
        return switch (goal) {
            case SAVE_MONEY ->
                    "Economizar dinheiro";

            case REDUCE_IMPULSE_PURCHASES ->
                    "Reduzir compras por impulso";

            case PLAN_A_PURCHASE ->
                    "Planejar uma compra";

            case ORGANIZE_BUDGET ->
                    "Organizar meu orçamento";
        };
    }

    private String formatTone(
            RecommendationTone tone
    ) {
        return switch (tone) {
            case GENTLE ->
                    "Gentil e acolhedor";

            case BALANCED ->
                    "Equilibrado";

            case DIRECT ->
                    "Direto e objetivo";
        };
    }

    private <T> StringConverter<T> createConverter(
            java.util.function.Function<T, String> formatter
    ) {
        return new StringConverter<>() {
            @Override
            public String toString(T value) {
                return value == null
                        ? ""
                        : formatter.apply(value);
            }

            @Override
            public T fromString(String text) {
                return null;
            }
        };
    }

    private BigDecimal parseOptionalMoney(
            String text
    ) {
        if (text == null || text.isBlank()) {
            return null;
        }

        try {
            String normalizedValue = text
                    .trim()
                    .replace("R$", "")
                    .replace("\u00A0", "")
                    .replace(" ", "");

            if (normalizedValue.contains(",")) {
                normalizedValue = normalizedValue
                        .replace(".", "")
                        .replace(",", ".");
            }

            return new BigDecimal(normalizedValue);

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Informe um valor válido para seu objetivo."
            );
        }
    }

    private void clearFeedback() {
        profileFeedbackLabel.setText("");

        profileFeedbackLabel.getStyleClass().setAll(
                "feedback-label"
        );
    }

    private void showSuccess(String message) {
        profileFeedbackLabel.setText(message);

        profileFeedbackLabel.getStyleClass().setAll(
                "feedback-label",
                "feedback-success"
        );
    }

    private void showError(String message) {
        profileFeedbackLabel.setText(
                message == null
                        ? "Verifique os dados informados."
                        : message
        );

        profileFeedbackLabel.getStyleClass().setAll(
                "feedback-label",
                "feedback-error"
        );
    }
}