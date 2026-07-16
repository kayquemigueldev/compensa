package com.kayque.compensa;

import com.kayque.compensa.database.DatabaseInitializer;
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
                new FadeTransition(
                        TRANSITION_DURATION,
                        splashView
                );

        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition pause =
                new PauseTransition(
                        SPLASH_DURATION
                );

        fadeIn.setOnFinished(
                event -> pause.play()
        );

        pause.setOnFinished(
                event -> showMainView(
                        scene,
                        splashView
                )
        );

        fadeIn.play();
    }

    private void showMainView(
            Scene scene,
            Parent splashView
    ) {
        FadeTransition fadeOut =
                new FadeTransition(
                        TRANSITION_DURATION,
                        splashView
                );

        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        fadeOut.setOnFinished(event -> {
            try {
                Parent mainView =
                        loadView("main-view.fxml");

                mainView.setOpacity(0);
                scene.setRoot(mainView);

                FadeTransition mainFadeIn =
                        new FadeTransition(
                                TRANSITION_DURATION,
                                mainView
                        );

                mainFadeIn.setFromValue(0);
                mainFadeIn.setToValue(1);
                mainFadeIn.play();

            } catch (IOException exception) {
                throw new IllegalStateException(
                        "Não foi possível abrir o aplicativo.",
                        exception
                );
            }
        });

        fadeOut.play();
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