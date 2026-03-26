package com.FrameHopper.app.ui.controller;

import com.FrameHopper.app.boundry.dto.TagDTO;
import com.FrameHopper.app.core.ports.in.tag.ChangeTagStatusCommand;
import com.FrameHopper.app.core.ports.in.tag.CreateTagCommand;
import com.FrameHopper.app.core.ports.in.tag.UpdateTagCommand;
import com.FrameHopper.app.ui.UiView;
import com.FrameHopper.app.ui.eventing.TagCreatedEventDispatcher;
import com.FrameHopper.app.ui.eventing.TagUpdatedEventDispatcher;
import com.FrameHopper.app.ui.language.I18n;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
    private final ObjectProperty<TagDTO> cachedTagProperty = new SimpleObjectProperty<>(null);

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
        bind(nameLabel, "td.label.name");
        bind(valueLabel, "td.label.value");
        bind(descriptionLabel, "td.label.description");

        setUpButton(cancelButton, "td.button.cancel", e -> close());
        setUpButton(saveButton, "td.button.save", e -> save());

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
        bind(button, label);
        button.setOnAction(event);
    }

    public void init(TagDTO tag) {
        cachedTagProperty.set(tag);
        var cachedTag = cachedTagProperty.get();

        changeStatusButton.textProperty().bind(
                Bindings.createStringBinding(
                        () -> {
                            var currentTag = cachedTagProperty.get();
                            return I18n.tr(
                                    currentTag != null && currentTag.getVisible()
                                            ? "td.button.status.hide"
                                            : "td.button.status.unhide"
                            );
                        },
                        I18n.localeProperty(),
                        cachedTagProperty
                )
        );
        changeStatusButton.setPrefHeight(25);
        changeStatusButton.setPrefWidth(120);
        changeStatusButton.setOnAction(e -> {
            var cachedTag2 = cachedTagProperty.get();
            cachedTag2.changeStatus();

            changeTagStatusCommand.ChangeTagStatus(cachedTag2.getId());
            cachedTagProperty.set(null);
            cachedTagProperty.set(cachedTag2);
            TagUpdatedEventDispatcher.dispatchUpdate(cachedTag2);
        });

        buttonBox.getChildren().addAll(cancelButton, changeStatusButton, saveButton);

        nameField.textProperty().setValue(cachedTag.getName());
        valueField.textProperty().setValue(String.valueOf(cachedTag.getValue()));
        descriptionArea.textProperty().setValue(cachedTag.getDescription());
    }

    public void init() {
        buttonBox.getChildren().addAll(cancelButton, saveButton);
    }

    private void save() {
        var cachedTag = cachedTagProperty.get();

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

        cachedTagProperty.set(cachedTag);

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
