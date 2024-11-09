package com.example.engineer.View.FXViews.TagDetails;

import com.example.engineer.Model.Tag;
import com.example.engineer.View.Elements.FXElementsProviders.FXDialogProvider;
import com.example.engineer.View.Elements.DataManagers.TagListManager;
import com.example.engineer.View.Elements.Language.LanguageChangeListener;
import com.example.engineer.View.Elements.Language.LanguageManager;
import com.example.engineer.View.Elements.UpdateTableEvent.UpdateTableEventDispatcher;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Component
@Scope("prototype")
public class TagDetailsController implements LanguageChangeListener {
    @FXML
    private TextField nameField;
    @FXML
    private TextField valueField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Button cancelButton;
    @FXML
    private Button hideButton;
    @FXML
    private Button saveButton;

    @Autowired
    TagDetailsService viewService;
    @Autowired
    TagListManager tagList;

    private InformationContainer info;

    @FXML
    public void initialize() {
        LanguageManager.register(this);
        // Button actions
        cancelButton.setOnAction(event -> closeWindow());
        hideButton.setOnAction(event -> toggleHide());
        saveButton.setOnAction(event -> {
            try{
                saveDetails();
            }catch (Exception e){
                FXDialogProvider.errorDialog(e.getMessage());
            }

        });

        Platform.runLater(() -> {
            var stage = (Stage) saveButton.getScene().getWindow();
            stage.setOnCloseRequest(e -> LanguageManager.unregister(this));
        });
    }

    public void init(int id){
        info = new InformationContainer(viewService.getTag(id));
        nameField.setText(info.getTag().getName());
        valueField.setText(String.valueOf(info.getTag().getValue()));
        descriptionArea.setText(info.getTag().getDescription());
        hideButton.setText(info.getTag().isDeleted() ? "Unhide" : "Hide");
    }

    private void toggleHide() {
        info.getTag().setDeleted(!info.getTag().isDeleted());
        hideButton.setText(info.getTag().isDeleted() ? "Unhide" : "Hide");
        tagList.changeHideStatus(info.getTag().getId(),info.getTag().isDeleted());
        UpdateTableEventDispatcher.fireEvent();
    }

    private void saveDetails() throws Exception{
        String name = nameField.getText();
        String value = valueField.getText();
        String description = descriptionArea.getText().isBlank() ? "" : descriptionArea.getText();

        viewService.updateTag(
                info.getTag(),
                name,
                value,
                description
        );

        closeWindow();
    }

    private void closeWindow() {
        LanguageManager.unregister(this);
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @Override
    public void changeLanguage() {

    }

    private static class InformationContainer{
        @Getter
        private final Tag tag;

        public InformationContainer(Tag tag) {
            this.tag = tag;
        }
    }
}
