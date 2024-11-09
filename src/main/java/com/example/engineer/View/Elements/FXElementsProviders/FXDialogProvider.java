package com.example.engineer.View.Elements.FXElementsProviders;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Arrays;

public class FXDialogProvider {
    //ERROR MESSAGES
    public static void errorDialog(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText("Error");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static void errorDialog(String message) {
        errorDialog("",message);
    }

    //YES/NO DIALOG
    public static boolean YesNoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.setHeaderText(null);

        var yesButton = new ButtonType("YES");
        var noButton = new ButtonType("NO");

        alert.getButtonTypes().setAll(yesButton, noButton);

        try{
            return alert.showAndWait().get().getText().equals("YES");
        }catch(Exception e){
            return false;
        }
    }

    public static boolean YesNoDialog(String message) {
        return YesNoDialog("",message);
    }

    //CUSTOM DIALOG
    public static int customDialog(String title, String message, int defaultOption, String... options) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.setHeaderText(null);

        var buttons = new ArrayList<ButtonType>();
        for (var option : options)
            buttons.add(new ButtonType(option));

        alert.getButtonTypes().setAll(buttons);

        try{
            var selected = alert.showAndWait().get().getText();
            var opt = Arrays.asList(options);

            return opt.indexOf(selected);
        }catch (Exception e){
            return defaultOption;
        }
    }

    public static int customDialog(String message, int defaultOptions,String... options) {
        return customDialog("",message,defaultOptions,options);
    }

    //MESSAGE DIALOG
    public static void messageDialog(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static void messageDialog(String message) {
        messageDialog("",message);
    }

    //GET PATH DIALOG
    public static String inputDialog(){
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Input File name");

        var okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        var cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(okButton, cancelButton);

        var textField = new TextField();
        textField.setPromptText("File Name");

        var content = new VBox();
        content.getChildren().add(textField);
        content.setSpacing(10);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(db -> db == cancelButton ? textField.getText() : null);

        return dialog.showAndWait().orElse(null);
    }
}
