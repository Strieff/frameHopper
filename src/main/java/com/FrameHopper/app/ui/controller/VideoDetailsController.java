package com.FrameHopper.app.ui.controller;

import com.FrameHopper.app.View.Elements.FXElementsProviders.FileChooserProvider;
import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.boundry.dto.VideoDTO;
import com.FrameHopper.app.core.ports.in.video.UpdateVideoPathCommand;
import com.FrameHopper.app.ui.UiView;
import com.FrameHopper.app.ui.eventing.VideoPathUpdatedEventDispatcher;
import com.FrameHopper.app.ui.eventing.VideoPathUpdatedListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Scope("prototype")
public class VideoDetailsController extends UiView implements
        VideoPathUpdatedListener
{
    @FXML
    private TextArea filePathField;
    @FXML
    private Label pathLabel,dataLabel,frameAmountLabel,frameRateLabel,durationLabel;
    @FXML
    private Button changeButton, closeButton;
    @FXML
    private BorderPane videoDetailsView;

    private final UpdateVideoPathCommand updateVideoPathCommand;

    private VideoDTO cachedVideo;

    public VideoDetailsController(UpdateVideoPathCommand updateVideoPathCommand) {
        this.updateVideoPathCommand = updateVideoPathCommand;

        VideoPathUpdatedEventDispatcher.register(this);
    }

    @FXML
    private void initialize() {
        videoDetailsView.setOnKeyPressed(e -> videoDetailsView.requestFocus());

        bind(pathLabel, "vd.path.label");
        bind(dataLabel, "vd.info.label");

        bind(changeButton, "vd.path.button");
        changeButton.setOnAction(e -> {
            try {
                var file = new File(FileChooserProvider.videoFileChooser((Stage) videoDetailsView.getScene().getWindow()));
                var path = file.getPath();

                cachedVideo = updateVideoPathCommand.updateVideoPath(cachedVideo, path);
                VideoPathUpdatedEventDispatcher.dispatch(cachedVideo);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        bind(closeButton, "vd.button.close");
        closeButton.setOnAction(e -> close());

        addKeybinds();

        Platform.runLater(() -> {
            var stage = (Stage) videoDetailsView.getScene().getWindow();
            stage.setOnCloseRequest(e -> close());
        });
    }

    public void init(VideoDTO videoDTO) {
        cachedVideo = videoDTO;

        bind(frameAmountLabel, "vd.info.frame-count", cachedVideo.metadata().totalFrames());
        bind(frameRateLabel, "vd.info.framerate", cachedVideo.metadata().frameRate());
        bind(durationLabel, "vd.info.duration", cachedVideo.metadata().duration());
        filePathField.setText(cachedVideo.path());
    }

    @Override
    public void addKeybinds() {
        keyActions.put(new KeyCodeCombination(KeyCode.C, KeyCombination.SHIFT_DOWN), this::close);

        addEventFilter(videoDetailsView);
    }

    @Override
    public void close() {
        VideoPathUpdatedEventDispatcher.unregister(this);

        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    @Override
    public void onVideoPathUpdated(VideoDTO video) {
        filePathField.setText(cachedVideo.path());
    }
}
