package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.ui.AppController;

import java.net.URISyntaxException;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        AppController app = new AppController();

        Scene scene = new Scene(app.getRoot(), 800, 600);
        String css = getClass().getResource("/style.css").toExternalForm();
        scene.getStylesheets().add(css);
        stage.setTitle("VETa");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
