package com.FrameHopper.app.View.FXViews.TagDetails;

import com.FrameHopper.app.Model.Tag;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXDialogProvider;
import com.FrameHopper.app.View.Elements.DataManagers.TagListManager;
import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.View.Elements.Language.LanguageChangeListener;
import com.FrameHopper.app.View.Elements.Language.LanguageManager;
import com.FrameHopper.app.View.Elements.UpdateTableEvent.UpdateTableEventDispatcher;
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
public class TagDetailsController implements LanguageChangeListener {
    @FXML
    private TextField nameField,valueField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Button cancelButton,hideButton,saveButton;
    @FXML
    private Label nameLabel,valueLabel,descriptionLabel;

    private final TagDetailsService viewService;
    private final TagListManager tagList;

    private InformationContainer info;

    public TagDetailsController(TagDetailsService viewService, TagListManager tagList) {
        this.viewService = viewService;
        this.tagList = tagList;

        LanguageManager.register(this);
    }

    @FXML
    public void initialize() {
        //labels
        nameLabel.setText(Dictionary.get("name")+":");
        valueLabel.setText(Dictionary.get("value")+":");
        descriptionLabel.setText(Dictionary.get("description")+":");

        // Button actions
        cancelButton.setOnAction(event -> close());
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
            stage.setOnCloseRequest(e -> close());
        });
    }

    public void init(int id){
        info = new InformationContainer(viewService.getTag(id));
        nameField.setText(info.tag().getName());
        valueField.setText(String.valueOf(info.tag().getValue()));
        descriptionArea.setText(info.tag().getDescription());
        hideButton.setText(Dictionary.get(info.tag().isDeleted() ? "td.unhide" : "td.hide"));
    }

    private void toggleHide() {
        info.tag().setDeleted(!info.tag().isDeleted());
        hideButton.setText(Dictionary.get(info.tag().isDeleted() ? "td.unhide" : "td.hide"));
        tagList.changeHideStatus(info.tag().getId(),info.tag().isDeleted());
        UpdateTableEventDispatcher.fireEvent();
    }

    private void saveDetails() throws Exception{
        String name = nameField.getText();
        String value = valueField.getText();
        String description = descriptionArea.getText().isBlank() ? "" : descriptionArea.getText();

        viewService.updateTag(
                info.tag(),
                name,
                value,
                description
        );

        close();
    }

    private void close() {
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
        hideButton.setText(Dictionary.get(info.tag().isDeleted() ? "td.unhide" : "td.hide"));
        saveButton.setText(Dictionary.get("save"));
    }

    private record InformationContainer(Tag tag) {
    }
}
