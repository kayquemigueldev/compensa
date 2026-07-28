package com.kayque.compensa;

import com.kayque.compensa.database.DatabaseInitializer;
import com.kayque.compensa.onboarding.controller.OnboardingController;
import com.kayque.compensa.onboarding.repository.SqliteAppPreferenceRepository;
import com.kayque.compensa.onboarding.service.OnboardingService;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class CompensaApplication extends Application {

    private static final double INITIAL_WIDTH = 1280;
    private static final double INITIAL_HEIGHT = 800;

    private static final Duration SPLASH_DURATION =
            Duration.seconds(2.8);

    private static final Duration TRANSITION_DURATION =
            Duration.millis(300);

    private final OnboardingService onboardingService =
            new OnboardingService(
                    new SqliteAppPreferenceRepository()
            );

    @Override
    public void start(Stage stage) throws IOException {
        DatabaseInitializer.initialize();

        Parent splashView =
                loadView("splash-view.fxml");

        splashView.setOpacity(0);

        Scene scene = new Scene(
                splashView,
                INITIAL_WIDTH,
                INITIAL_HEIGHT
        );

        scene.getStylesheets().add(
                CompensaApplication.class
                        .getResource("styles.css")
                        .toExternalForm()
        );

        stage.setTitle("Compensa?");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(680);

        stage.show();
        stage.centerOnScreen();

        playSplashAnimation(
                scene,
                splashView
        );
    }

    private void playSplashAnimation(
            Scene scene,
            Parent splashView
    ) {
        FadeTransition fadeIn =
                createFadeTransition(
                        splashView,
                        0,
                        1
                );

        PauseTransition pause =
                new PauseTransition(
                        SPLASH_DURATION
                );

        fadeIn.setOnFinished(
                event -> pause.play()
        );

        pause.setOnFinished(
                event -> showInitialDestination(
                        scene,
                        splashView
                )
        );

        fadeIn.play();
    }

    private void showInitialDestination(
            Scene scene,
            Parent splashView
    ) {
        FadeTransition fadeOut =
                createFadeTransition(
                        splashView,
                        1,
                        0
                );

        fadeOut.setOnFinished(event -> {
            try {
                if (onboardingService
                        .shouldShowOnboarding()) {
                    showOnboarding(scene);
                } else {
                    showMainView(scene);
                }

            } catch (IOException exception) {
                throw new IllegalStateException(
                        "Não foi possível abrir o aplicativo.",
                        exception
                );
            }
        });

        fadeOut.play();
    }

    private void showOnboarding(
            Scene scene
    ) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                CompensaApplication.class.getResource(
                        "onboarding/onboarding-view.fxml"
                )
        );

        Parent onboardingView = loader.load();

        OnboardingController controller =
                loader.getController();

        controller.setOnFinished(
                () -> completeOnboarding(scene)
        );

        showViewWithFade(
                scene,
                onboardingView
        );
    }

    private void completeOnboarding(Scene scene) {
        onboardingService.completeOnboarding();

        try {
            transitionToMainView(scene);

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Não foi possível iniciar o aplicativo.",
                    exception
            );
        }
    }

    private void transitionToMainView(
            Scene scene
    ) throws IOException {
        Parent currentView = scene.getRoot();

        FadeTransition fadeOut =
                createFadeTransition(
                        currentView,
                        1,
                        0
                );

        fadeOut.setOnFinished(event -> {
            try {
                showMainView(scene);

            } catch (IOException exception) {
                throw new IllegalStateException(
                        "Não foi possível abrir a tela principal.",
                        exception
                );
            }
        });

        fadeOut.play();
    }

    private void showMainView(
            Scene scene
    ) throws IOException {
        Parent mainView =
                loadView("main-view.fxml");

        showViewWithFade(
                scene,
                mainView
        );
    }

    private void showViewWithFade(
            Scene scene,
            Parent view
    ) {
        view.setOpacity(0);
        scene.setRoot(view);

        FadeTransition fadeIn =
                createFadeTransition(
                        view,
                        0,
                        1
                );

        fadeIn.play();
    }

    private FadeTransition createFadeTransition(
            Parent view,
            double fromValue,
            double toValue
    ) {
        FadeTransition transition =
                new FadeTransition(
                        TRANSITION_DURATION,
                        view
                );

        transition.setFromValue(fromValue);
        transition.setToValue(toValue);

        return transition;
    }

    private Parent loadView(
            String resource
    ) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                CompensaApplication.class
                        .getResource(resource)
        );

        return loader.load();
    }
}