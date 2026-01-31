package com.FrameHopper.app.View.FXViews.VideoDetails;

import com.FrameHopper.app.Model.Video;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXDialogProvider;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FileChooserProvider;
import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.View.Elements.Language.LanguageChangeListener;
import com.FrameHopper.app.View.Elements.Language.LanguageManager;
import com.FrameHopper.app.View.Elements.UpdateTableEvent.UpdateTableEventDispatcher;
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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Component
@Scope("prototype")
public class VideoManagementDetailsController implements LanguageChangeListener {
    @FXML
    private TextArea filePathField;
    @FXML
    private Label pathLabel,dataLabel,frameAmountLabel,frameRateLabel,durationLabel;
    @FXML
    private Button changeButton, closeButton;
    @FXML
    private BorderPane videoDetailsView;

    private final VideoManagementDetailsService viewService;
    private final Map<KeyCombination,Runnable> keyActions = new HashMap<>();

    private Video video;

    public VideoManagementDetailsController(VideoManagementDetailsService viewService) {
        this.viewService = viewService;

        LanguageManager.register(this);
    }

    @FXML
    public void initialize() {
        pathLabel.setText(Dictionary.get("vd.path"));
        dataLabel.setText(Dictionary.get("vd.data"));

        // Set button actions
        changeButton.setOnAction(event -> changePath());
        changeButton.setText(Dictionary.get("vd.change"));
        closeButton.setOnAction(event -> close());
        closeButton.setText(Dictionary.get("close"));

        keyActions.put(new KeyCodeCombination(KeyCode.D, KeyCombination.SHIFT_DOWN), this::close);

        //add key binds
        videoDetailsView.addEventFilter(KeyEvent.KEY_PRESSED,this::handleKeyPressed);

        Platform.runLater(() -> {
            var stage = (Stage) videoDetailsView.getScene().getWindow();
            stage.setOnCloseRequest(e -> close());
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
        this.video = video;
        frameAmountLabel.setText(Dictionary.get("vd.frameAmount")+video.getTotalFrames());
        frameRateLabel.setText(Dictionary.get("vd.framerate")+video.getFrameRate());
        durationLabel.setText(Dictionary.get("vd.duration")+video.getDuration());
        filePathField.setText(video.getPath());
    }

    private void changePath() {
        try {
            var file = new File(FileChooserProvider.videoFileChooser((Stage) videoDetailsView.getScene().getWindow()));
            var pathExists = viewService.getVideoByPath(file.getPath()) != null;

            if(pathExists)
                throw new Exception(Dictionary.get("warning.path.exists"));

            video.setPath(file.getPath());
            video.setName(file.getName().replace(" ", "%20"));
            viewService.saveVideo(video);

            filePathField.setText(video.getPath());

            UpdateTableEventDispatcher.fireEvent();
        } catch (Exception e) {
            FXDialogProvider.errorDialog(e.getMessage());
            e.printStackTrace();
        }
    }

    public void close() {
        LanguageManager.unregister(this);
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    @Override
    public void changeLanguage() {
        pathLabel.setText(Dictionary.get("vd.path"));
        dataLabel.setText(Dictionary.get("vd.data"));
        changeButton.setText(Dictionary.get("vd.change"));
        closeButton.setText(Dictionary.get("close"));

        frameAmountLabel.setText(Dictionary.get("vd.frameAmount") + video.getTotalFrames());
        frameRateLabel.setText(Dictionary.get("vd.framerate") + video.getFrameRate());
        durationLabel.setText(Dictionary.get("vd.duration") + video.getDuration());
    }
}
