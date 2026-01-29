package com.FrameHopper.app.View.FXViews.CreateTag;

import com.FrameHopper.app.View.Elements.DataManagers.ViewContainer.OpenViewsInformationContainer;
import com.FrameHopper.app.View.Elements.DataManagers.ViewContainer.ViewFlag;
import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.View.Elements.Language.LanguageChangeListener;
import com.FrameHopper.app.View.Elements.Language.LanguageManager;
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
public class CreateTagController implements LanguageChangeListener {
    @FXML
    private TextField nameField,valueField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Button cancelButton,saveButton;
    @FXML
    private Label nameLabel,valueLabel,descriptionLabel;

    private final CreateTagService viewService;
    private final  OpenViewsInformationContainer viewContainer;

    public CreateTagController(CreateTagService viewService, OpenViewsInformationContainer viewContainer) {
        this.viewService = viewService;
        this.viewContainer = viewContainer;

        LanguageManager.register(this);
    }

    @FXML
    public void initialize() {
        //labels
        nameLabel.setText(Dictionary.get("name")+":");
        valueLabel.setText(Dictionary.get("value")+":");
        descriptionLabel.setText(Dictionary.get("description")+":");

        // Button actions
        cancelButton.setOnAction(event -> closeWindow());
        cancelButton.setText(Dictionary.get("cancel"));
        saveButton.setOnAction(event -> saveTag());
        saveButton.setText(Dictionary.get("save"));

        Platform.runLater(() -> {
            var stage = (Stage) nameField.getScene().getWindow();
            stage.setOnCloseRequest(e -> {
                LanguageManager.unregister(this);
                viewContainer.close(ViewFlag.CREATE_TAG);
            });
        });
    }

    private void saveTag() {
        viewService.createTag(
            nameField.getText(),
            valueField.getText(),
            descriptionArea.getText()
        );

        closeWindow();
    }

    private void closeWindow() {
        LanguageManager.unregister(this);
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
        viewContainer.close(ViewFlag.CREATE_TAG);
    }

    @Override
    public void changeLanguage() {
        nameLabel.setText(Dictionary.get("name")+":");
        valueLabel.setText(Dictionary.get("value")+":");
        descriptionLabel.setText(Dictionary.get("description")+":");
        cancelButton.setText(Dictionary.get("cancel"));
        saveButton.setText(Dictionary.get("save"));
    }
}
