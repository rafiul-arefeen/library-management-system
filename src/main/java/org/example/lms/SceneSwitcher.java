package org.example.lms;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class SceneSwitcher {

    public static void switchScene(ActionEvent event, String fxmlFileName, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource(fxmlFileName));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root, 1280, 768));
            stage.setResizable(false);
            stage.setTitle(title);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
