package com.example.engineer.View.Elements.FXElementsProviders;

import com.example.engineer.View.Elements.Language.Dictionary;
import com.example.engineer.View.Elements.Language.LanguageEntry;
import com.example.engineer.View.Elements.Language.LanguageManager;
import com.example.engineer.View.FXViews.Settings.LanguageCell;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FXDialogProvider {
    //ERROR MESSAGES
    public static void errorDialog(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(Dictionary.get("error"));
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static void errorDialog(String message) {
        errorDialog("",message);
    }

    //YES/NO DIALOG
    public static boolean yesNoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.setHeaderText(null);

        var yesButton = new ButtonType(Dictionary.get("yes"));
        var noButton = new ButtonType(Dictionary.get("no"));

        alert.getButtonTypes().setAll(yesButton, noButton);

        try{
            return alert.showAndWait().get().getText().equals(Dictionary.get("yes"));
        }catch(Exception e){
            return false;
        }
    }

    public static boolean yesNoDialog(String message) {
        return yesNoDialog("",message);
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

        var okButton = new ButtonType(Dictionary.get("ok"), ButtonBar.ButtonData.OK_DONE);
        var cancelButton = new ButtonType(Dictionary.get("cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(okButton, cancelButton);

        var textField = new TextField();
        textField.setPromptText(Dictionary.get("dialog.input.fileName"));

        var content = new VBox();
        content.getChildren().add(textField);
        content.setSpacing(10);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(db -> db == okButton ? textField.getText() : null);

        return dialog.showAndWait().orElse(null);
    }

    //GET LANGUAGE DIALOG
    public static String languageDialog(){
        Dialog<String> dialog = new Dialog<>();

        var okButton = new ButtonType(Dictionary.get("ok"), ButtonBar.ButtonData.OK_DONE);
        var cancelButton = new ButtonType(Dictionary.get("cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(okButton, cancelButton);

        List<LanguageEntry> languages = new ArrayList<>();
        LanguageManager.getLanguages().forEach((key,value) -> languages.add(new LanguageEntry(key,value)));

        ComboBox<LanguageEntry> languageBox = new ComboBox<>();
        languageBox.getItems().addAll(languages);
        languageBox.setCellFactory(cb -> new LanguageCell());
        languageBox.setButtonCell(new LanguageCell());
        languageBox.getSelectionModel().select(languages.stream().filter(i -> i.getCode().equals("en")).findFirst().get());

        var content = new VBox();
        content.setAlignment(Pos.CENTER);
        content.getChildren().add(languageBox);
        content.setSpacing(10);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(db -> db == okButton ? languageBox.getSelectionModel().getSelectedItem().getCode() : "en");

        return dialog.showAndWait().orElse(null);
    }
}
