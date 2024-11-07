package com.example.engineer.View.FXViews.TagDetails;

import com.example.engineer.Model.Tag;
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

    private InformationContainer info;

    @FXML
    public void initialize() {
        // Button actions
        cancelButton.setOnAction(event -> closeWindow());
        hideButton.setOnAction(event -> toggleHide());
        saveButton.setOnAction(event -> saveDetails());
    }

    public void init(int id){
        info = new InformationContainer(viewService.getTag(id));
        nameField.setText(info.getTag().getName());
        valueField.setText(String.valueOf(info.getTag().getValue()));
        descriptionArea.setText(info.getTag().getDescription());
        hideButton.setText(info.getTag().isDeleted() ? "Unhide" : "Hide");
    }

    //TODO: change in DB
    private void toggleHide() {
        info.getTag().setDeleted(!info.getTag().isDeleted());
        hideButton.setText(info.getTag().isDeleted() ? "Unhide" : "Hide");
        //TODO: fire event
    }

    private void saveDetails() {
        String name = nameField.getText();
        String value = valueField.getText();
        String description = descriptionArea.getText();

        //TODO: Handle save logic, e.g., updating database or data source
        closeWindow();

        //TODO: fire event if there is any difference
        System.out.println(info.getTag().toString());
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
