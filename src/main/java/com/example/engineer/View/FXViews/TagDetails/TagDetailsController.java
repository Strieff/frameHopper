package com.example.engineer.View.FXViews.TagDetails;

import com.example.engineer.Model.Tag;
import com.example.engineer.View.Elements.FXDialogProvider;
import com.example.engineer.View.Elements.TagListManager;
import com.example.engineer.View.Elements.UpdateTableEvent.UpdateTableEventDispatcher;
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
public class TagDetailsController {
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
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private static class InformationContainer{
        @Getter
        private final Tag tag;

        public InformationContainer(Tag tag) {
            this.tag = tag;
        }
    }
}
