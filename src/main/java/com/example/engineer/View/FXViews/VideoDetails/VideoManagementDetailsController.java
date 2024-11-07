package com.example.engineer.View.FXViews.VideoDetails;

import com.example.engineer.Model.Video;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class VideoManagementDetailsController {
    @FXML
    private TextArea filePathField;
    @FXML
    private Label frameAmountLabel;
    @FXML
    private Label frameRateLabel;
    @FXML
    private Label durationLabel;
    @FXML
    private Button changeButton;
    @FXML
    private Button closeButton;

    @Autowired
    VideoManagementDetailsService viewService;

    private int id;

    @FXML
    public void initialize() {
        // Set button actions
        changeButton.setOnAction(event -> changePath());
        closeButton.setOnAction(event -> closeWindow());
    }

    public void init(Video video){
        id = video.getId();
        frameAmountLabel.setText("Frame amount: "+video.getTotalFrames());
        frameRateLabel.setText("Framerate: "+video.getFrameRate());
        durationLabel.setText("Duration: "+video.getDuration());
        filePathField.setText(video.getPath());
    }

    private void changePath() {
        viewService.changePath(id,(Stage)closeButton.getScene().getWindow());
    }

    private void closeWindow() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
