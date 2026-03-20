package com.FrameHopper.app.ui.controller;

import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.boundry.dto.TagDTO;
import com.FrameHopper.app.core.ports.in.tag.ChangeTagStatusCommand;
import com.FrameHopper.app.core.ports.in.tag.CreateTagCommand;
import com.FrameHopper.app.core.ports.in.tag.UpdateTagCommand;
import com.FrameHopper.app.ui.UiView;
import com.FrameHopper.app.ui.eventing.TagCreatedEventDispatcher;
import com.FrameHopper.app.ui.eventing.TagUpdatedEventDispatcher;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class TagDetailsController extends UiView {
    @FXML
    private TextField nameField,valueField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Label nameLabel,valueLabel,descriptionLabel;
    @FXML
    private HBox buttonBox;
    @FXML
    private BorderPane tagDetailsView;

    private final CreateTagCommand createTagCommand;
    private final ChangeTagStatusCommand changeTagStatusCommand;
    private final UpdateTagCommand updateTagCommand;

    private final Button cancelButton, changeStatusButton, saveButton;
    private TagDTO cachedTag;

    public TagDetailsController(
            CreateTagCommand createTagCommand,
            ChangeTagStatusCommand changeTagStatusCommand,
            UpdateTagCommand updateTagCommand
    ) {
        this.createTagCommand = createTagCommand;
        this.changeTagStatusCommand = changeTagStatusCommand;
        this.updateTagCommand = updateTagCommand;

        cancelButton =  new Button();
        changeStatusButton =  new Button();
        saveButton =  new Button();
    }

    @FXML
    private void initialize() {
        tagDetailsView.setOnMouseClicked(e -> tagDetailsView.requestFocus());

        //labels
        nameLabel.setText(Dictionary.get("name")+":");
        valueLabel.setText(Dictionary.get("value")+":");
        descriptionLabel.setText(Dictionary.get("description")+":");

        setUpButton(cancelButton, "cancel", e -> close());
        setUpButton(saveButton, "save", e -> save());

        setUpButton(
                changeStatusButton,
                "",
                e -> {
                    cachedTag.changeStatus();
                    changeTagStatusCommand.ChangeTagStatus(cachedTag.getId());
                    changeStatusButton.setText(Dictionary.get(cachedTag.getVisible() ? "td.hide" : "td.unhide"));

                    TagUpdatedEventDispatcher.dispatchUpdate(cachedTag);
                }
        );

        addKeybinds();

        Platform.runLater(() -> {
            tagDetailsView.requestFocus();

            var stage = (Stage) tagDetailsView.getScene().getWindow();
            stage.setOnCloseRequest(e -> close());
        });
    }

    private void setUpButton(Button button, String label, EventHandler<ActionEvent> event) {
        button.setPrefHeight(25);
        button.setPrefWidth(120);
        button.setText(Dictionary.get(label));
        button.setOnAction(event);
    }

    public void init(TagDTO tag) {
        cachedTag = tag;

        changeStatusButton.setText(Dictionary.get(cachedTag.getVisible() ? "td.hide" : "td.unhide"));
        buttonBox.getChildren().addAll(cancelButton, changeStatusButton, saveButton);

        nameField.textProperty().setValue(cachedTag.getName());
        valueField.textProperty().setValue(String.valueOf(cachedTag.getValue()));
        descriptionArea.textProperty().setValue(cachedTag.getDescription());
    }

    public void init() {
        buttonBox.getChildren().addAll(cancelButton, saveButton);
    }

    private void save() {
        try {
            Double.parseDouble(valueField.getText());
        }  catch (Exception ex) {
            //TODO exception
            return;
        }

        var name = nameField.getText();
        var value = Double.parseDouble(valueField.getText());
        var description = descriptionArea.getText().isBlank() ? "" : descriptionArea.getText();

        if(cachedTag==null) {
            cachedTag = createTagCommand.CreateTag(new TagDTO(
                    name,
                    value,
                    description
            ));

            TagCreatedEventDispatcher.dispatchCreate(cachedTag);
        }
        else {
            cachedTag.setName(name);
            cachedTag.setValue(value);
            cachedTag.setDescription(description);

            updateTagCommand.UpdateTag(cachedTag);

            TagUpdatedEventDispatcher.dispatchUpdate(cachedTag);
        }

        close();
    }

    @Override
    public void addKeybinds() {
        keyActions.put(new KeyCodeCombination(KeyCode.C, KeyCombination.SHIFT_DOWN), this::close);
        keyActions.put(new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN), this::save);

        addEventFilter(tagDetailsView);
    }

    @Override
    public void close() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
