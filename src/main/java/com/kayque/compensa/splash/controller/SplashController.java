package com.kayque.compensa.splash.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.ProgressBar;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Arc;
import javafx.util.Duration;


public class SplashController {

    @FXML
    private HBox brandContainer;

    @FXML
    private Arc logoArc;

    @FXML
    private Label wordmarkLabel;

    @FXML
    private Label splashSubtitleLabel;

    @FXML
    private ProgressBar splashProgressBar;

    @FXML
    private void initialize() {
        logoArc.setOpacity(0);
        logoArc.setScaleX(0.65);
        logoArc.setScaleY(0.65);

        wordmarkLabel.setOpacity(0);
        splashSubtitleLabel.setOpacity(0);
        splashProgressBar.setProgress(0);

        /*
         * Compensa o espaço ocupado pelo texto invisível,
         * deixando inicialmente apenas o C no centro.
         */
        brandContainer.setTranslateX(105);

        playAnimation();
    }

    private void playAnimation() {
        FadeTransition showLogo =
                new FadeTransition(
                        Duration.millis(650),
                        logoArc
                );

        showLogo.setFromValue(0);
        showLogo.setToValue(1);

        ScaleTransition growLogo =
                new ScaleTransition(
                        Duration.millis(650),
                        logoArc
                );

        growLogo.setToX(1);
        growLogo.setToY(1);
        growLogo.setInterpolator(
                Interpolator.EASE_OUT
        );

        ParallelTransition revealLogo =
                new ParallelTransition(
                        showLogo,
                        growLogo
                );

        PauseTransition logoPause =
                new PauseTransition(
                        Duration.millis(250)
                );

        TranslateTransition moveBrand =
                new TranslateTransition(
                        Duration.millis(650),
                        brandContainer
                );

        moveBrand.setToX(0);
        moveBrand.setInterpolator(
                Interpolator.EASE_BOTH
        );

        FadeTransition showWordmark =
                new FadeTransition(
                        Duration.millis(650),
                        wordmarkLabel
                );

        showWordmark.setFromValue(0);
        showWordmark.setToValue(1);

        ParallelTransition completeName =
                new ParallelTransition(
                        moveBrand,
                        showWordmark
                );

        FadeTransition showSubtitle =
                new FadeTransition(
                        Duration.millis(450),
                        splashSubtitleLabel
                );

        showSubtitle.setFromValue(0);
        showSubtitle.setToValue(1);

        SequentialTransition animation =
                new SequentialTransition(
                        revealLogo,
                        logoPause,
                        completeName,
                        showSubtitle
                );

        Timeline progressAnimation =
                new Timeline(
                        new KeyFrame(
                                Duration.ZERO,
                                new KeyValue(
                                        splashProgressBar.progressProperty(),
                                        0
                                )
                        ),
                        new KeyFrame(
                                Duration.millis(2200),
                                new KeyValue(
                                        splashProgressBar.progressProperty(),
                                        1,
                                        Interpolator.EASE_BOTH
                                )
                        )
                );

        progressAnimation.play();
        animation.play();
    }
}