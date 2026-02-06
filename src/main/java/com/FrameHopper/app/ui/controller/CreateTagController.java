package com.FrameHopper.app.ui.controller;

import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.core.ports.in.tag.CreateTagCommand;
import com.FrameHopper.app.boundry.dto.TagDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class CreateTagController {
    @FXML
    private TextField nameField, valueField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Button cancelButton, saveButton;
    @FXML
    private Label nameLabel, valueLabel, descriptionLabel;

    private final CreateTagCommand createTagCommand;

    public CreateTagController(CreateTagCommand createTagCommand) {
        this.createTagCommand = createTagCommand;
    }

    @FXML
    public void initialize() {
        //labels
        nameLabel.setText(Dictionary.get("name")+":");
        valueLabel.setText(Dictionary.get("value")+":");
        descriptionLabel.setText(Dictionary.get("description")+":");

        // Button actions
        cancelButton.setOnAction(e -> close());
        cancelButton.setText(Dictionary.get("cancel"));
        saveButton.setOnAction(e -> tryCreateTag());
        saveButton.setText(Dictionary.get("save"));

        Platform.runLater(() -> {
            var stage = (Stage) nameField.getScene().getWindow();
            stage.setOnCloseRequest(e -> close());
        });
    }

    private void close() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void tryCreateTag() {
        Double value;
        try {
            value = Double.parseDouble(valueField.getText());
        } catch (NumberFormatException e) {
            //TODO: error
            return;
        }

        var dto = new TagDTO(
                -1,
                nameField.getText(),
                value,
                descriptionArea.getText(),
                true
        );

        //TODO: try
        var tag = createTagCommand.CreateTag(dto);

        close();
    }
}
