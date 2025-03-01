package com.example.engineer.View.FXViews.VideoMerge;

import com.example.engineer.Model.Video;
import com.example.engineer.View.Elements.Language.Dictionary;
import com.example.engineer.View.Elements.Language.LanguageChangeListener;
import com.example.engineer.View.Elements.Language.LanguageManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Scope("prototype")
public class VideoMetadataMergeController implements LanguageChangeListener {
    @FXML
    private BorderPane metadataComparisonView;
    @FXML
    private Label titleLabel,oldDataLabel,newDataLabel,conflictLabel;
    @FXML
    private Button cancelButton,continueButton;

    @Autowired
    private VideoMergeService viewService;

    private Video oldVideo;
    private Video newVideo;

    @FXML
    public void initialize() {
        LanguageManager.register(this);

        titleLabel.setText(Dictionary.get("mv.metadata"));

        cancelButton.setText(Dictionary.get("cancel"));
        cancelButton.setOnAction(event -> closeWindow());
        continueButton.setText(Dictionary.get("continue"));


        oldDataLabel.setWrapText(true);
        newDataLabel.setWrapText(true);

        Platform.runLater(() -> {
            var stage = (Stage) metadataComparisonView.getScene().getWindow();
            stage.setOnCloseRequest(e -> LanguageManager.unregister(this));
        });
    }

    public void init(int id,File newFile){
        oldVideo = viewService.getVideo(id);
        newVideo = viewService.getVideoFromFile(newFile);

        oldDataLabel.setText(viewService.getVideoData(oldVideo,true));
        newDataLabel.setText(viewService.getVideoData(newVideo,false));

        var conflictData = viewService.compareMetadata(oldVideo, newVideo);
        if(conflictData.isEmpty())
            conflictLabel.setText(Dictionary.get("mv.metadata.no-conflict"));
        else {
            conflictLabel.setText(String.format(Dictionary.get("mv.metadata.conflict"), conflictData));
            conflictLabel.setTextFill(Color.RED);
        }
    }

    private void closeWindow() {
        LanguageManager.unregister(this);
        Stage stage = (Stage) metadataComparisonView.getScene().getWindow();
        stage.close();
    }

    @Override
    public void changeLanguage() {
        titleLabel.setText(Dictionary.get("mv.metadata"));

        oldDataLabel.setText(viewService.getVideoData(oldVideo,true));
        newDataLabel.setText(viewService.getVideoData(newVideo,false));


        var conflictData = viewService.compareMetadata(oldVideo, newVideo);
        if(conflictData.isEmpty())
            conflictLabel.setText(Dictionary.get("mv.metadata.no-conflict"));
        else {
            conflictLabel.setText(String.format(Dictionary.get("mv.metadata.conflict"), conflictData));
            conflictLabel.setTextFill(Color.RED);
        }

        cancelButton.setText(Dictionary.get("cancel"));
        continueButton.setText(Dictionary.get("continue"));
    }
}
