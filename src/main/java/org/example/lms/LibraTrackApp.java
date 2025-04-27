package org.example.lms;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LibraTrackApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/lms/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1280, 768);

        // ✅ Fixed path with leading slash
        scene.getStylesheets().add(
                LibraTrackApp.class.getResource("/org/example/lms/style.css").toExternalForm()
        );

        stage.setTitle("LibraTrack");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
