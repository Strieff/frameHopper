package com.example.engineer.View.FXViews.TagDetails;

import com.example.engineer.Model.Tag;
import com.example.engineer.View.Elements.FXElementsProviders.FXDialogProvider;
import com.example.engineer.View.Elements.DataManagers.TagListManager;
import com.example.engineer.View.Elements.Language.Dictionary;
import com.example.engineer.View.Elements.Language.LanguageChangeListener;
import com.example.engineer.View.Elements.Language.LanguageManager;
import com.example.engineer.View.Elements.UpdateTableEvent.UpdateTableEventDispatcher;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
    private TextField nameField,valueField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Button cancelButton,hideButton,saveButton;
    @FXML
    private Label nameLabel,valueLabel,descriptionLabel;

    @Autowired
    TagDetailsService viewService;
    @Autowired
    TagListManager tagList;

    private InformationContainer info;

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
        hideButton.setOnAction(event -> toggleHide());
        saveButton.setOnAction(event -> {
            try{
                saveDetails();
            }catch (Exception e){
                FXDialogProvider.errorDialog(e.getMessage());
            }

        });
        saveButton.setText(Dictionary.get("save"));

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
        hideButton.setText(Dictionary.get(info.getTag().isDeleted() ? "td.unhide" : "td.hide"));
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
        nameLabel.setText(Dictionary.get("name")+":");
        valueLabel.setText(Dictionary.get("value")+":");
        descriptionLabel.setText(Dictionary.get("description")+":");
        cancelButton.setText(Dictionary.get("cancel"));
        hideButton.setText(Dictionary.get(info.getTag().isDeleted() ? "td.unhide" : "td.hide"));
        saveButton.setText(Dictionary.get("save"));
    }

    private static class InformationContainer{
        @Getter
        private final Tag tag;

        public InformationContainer(Tag tag) {
            this.tag = tag;
        }
    }
}
