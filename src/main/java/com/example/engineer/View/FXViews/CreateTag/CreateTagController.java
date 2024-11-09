package com.example.engineer.View.FXViews.CreateTag;

import com.example.engineer.View.Elements.DataManagers.OpenViewsInformationContainer;
import com.example.engineer.View.Elements.Language.LanguageChangeListener;
import com.example.engineer.View.Elements.Language.LanguageManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
    private TextField nameField;
    @FXML
    private TextField valueField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Button cancelButton;
    @FXML
    private Button saveButton;

    @Autowired
    CreateTagService viewService;
    @Autowired
    private OpenViewsInformationContainer openViews;


    @FXML
    public void initialize() {
        LanguageManager.register(this);

        // Button actions
        cancelButton.setOnAction(event -> closeWindow());
        saveButton.setOnAction(event -> saveTag());

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
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
        openViews.closeCreateTag();
    }

    @Override
    public void changeLanguage() {

    }
}
