package com.example.engineer.View.FXViews.CreateTag;

import com.example.engineer.View.Elements.DataManagers.OpenViewsInformationContainer;
import com.example.engineer.View.Elements.Language.Dictionary;
import com.example.engineer.View.Elements.Language.LanguageChangeListener;
import com.example.engineer.View.Elements.Language.LanguageManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    CreateTagService viewService;
    @Autowired
    private OpenViewsInformationContainer openViews;


    @FXML
    public void initialize() {
        LanguageManager.register(this);

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
                openViews.closeCreateTag();
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
        openViews.closeCreateTag();
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
