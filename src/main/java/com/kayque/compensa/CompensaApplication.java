package com.kayque.compensa;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class CompensaApplication extends Application {

    private static final double INITIAL_WIDTH = 1280;
    private static final double INITIAL_HEIGHT = 800;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                CompensaApplication.class.getResource("main-view.fxml")
        );

        Scene scene = new Scene(
                loader.load(),
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

        stage.centerOnScreen();
        stage.show();
    }
}