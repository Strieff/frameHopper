package com.example.engineer.View.FXViews.VideoDetails;

import com.example.engineer.Model.Video;
import com.example.engineer.View.Elements.Language.LanguageChangeListener;
import com.example.engineer.View.Elements.Language.LanguageManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope("prototype")
public class VideoManagementDetailsController implements LanguageChangeListener {
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
    @FXML
    private BorderPane videoDetailsView;

    @Autowired
    VideoManagementDetailsService viewService;

    private int id;
    private final Map<KeyCombination,Runnable> keyActions = new HashMap<>();

    @FXML
    public void initialize() {
        LanguageManager.register(this);

        // Set button actions
        changeButton.setOnAction(event -> changePath());
        closeButton.setOnAction(event -> closeWindow());

        keyActions.put(new KeyCodeCombination(KeyCode.D, KeyCombination.SHIFT_DOWN), this::onShiftDPressed);

        //add key binds
        videoDetailsView.addEventFilter(KeyEvent.KEY_PRESSED,this::handleKeyPressed);

        Platform.runLater(() -> {
            var stage = (Stage) videoDetailsView.getScene().getWindow();
            stage.setOnCloseRequest(e -> LanguageManager.unregister(this));
        });
    }

    //HANDLE KEY BINDS
    private void handleKeyPressed(KeyEvent event){
        keyActions.keySet().stream()
                .filter(k -> k.match(event))
                .findFirst()
                .ifPresent(k -> keyActions.get(k).run());
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

    private void onShiftDPressed() {
        var stage = (Stage)closeButton.getScene().getWindow();
        stage.close();
    }

    private void closeWindow() {
        LanguageManager.unregister(this);
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    @Override
    public void changeLanguage() {

    }
}
