package com.example.engineer.View.FXViews.CreateTag;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CreateTagController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField valueField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Button cancelButton;
    @FXML
    private Button saveButton;

    @FXML
    public void initialize() {
        // Initialize fields with example data (could be replaced with actual data)
        nameField.setText("test1");
        valueField.setText("27.0");
        descriptionArea.setText("ab1");

        // Button actions
        cancelButton.setOnAction(event -> closeWindow());
        saveButton.setOnAction(event -> saveDetails());
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void saveDetails() {
        String name = nameField.getText();
        String value = valueField.getText();
        String description = descriptionArea.getText();

        // Handle save logic, e.g., updating database or data source
        System.out.println("Saved details:");
        System.out.println("Name: " + name);
        System.out.println("Value: " + value);
        System.out.println("Description: " + description);

        closeWindow();
    }
}
