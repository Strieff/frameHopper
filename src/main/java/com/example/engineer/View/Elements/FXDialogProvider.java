package com.example.engineer.View.Elements;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public class FXDialogProvider {
    public static void showErrorDialog(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText("Error");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static void showErrorDialog(String message) {
        showErrorDialog("",message);
    }
}
