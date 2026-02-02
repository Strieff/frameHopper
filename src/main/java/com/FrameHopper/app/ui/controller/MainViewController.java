package com.FrameHopper.app.ui.controller;

import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.View.FXViews.MainView.TableEntry;
import com.FrameHopper.app.core.domain.Video;
import com.FrameHopper.app.core.ports.in.FrameQuery;
import com.FrameHopper.app.core.ports.in.tag.TagsQuery;
import com.FrameHopper.app.core.ports.in.video.LoadVideoCommand;
import com.FrameHopper.app.core.ports.in.video.VideoMetadataQuery;
import com.FrameHopper.app.ui.ve.MainViewTagTableEntry;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Scope("prototype")
public class MainViewController {
    private final TagsQuery tagsQuery;
    @FXML
    private TextField frameInput;
    @FXML
    private Label dropLabel, statusLabel;
    @FXML
    private TableView<MainViewTagTableEntry> tableView;
    @FXML
    private TableColumn<MainViewTagTableEntry,String> nameColumn;
    @FXML
    private TableColumn<MainViewTagTableEntry,Double> valueColumn;
    @FXML
    private ImageView
            addButtonIcon,
            settingsButtonIcon,
            exportButtonIcon,
            chartButtonIcon,
            tagManagerIcon,
            notesButtonIcon,
            videoListButtonIcon;
    @FXML
    private BorderPane mainView;
    @FXML
    private Button jumpButton;
    @FXML
    private ImageView frameView;
    @FXML
    private StackPane framePane;

    private final Map<KeyCombination,Runnable> keyActions = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(MainViewController.class);

    private final LoadVideoCommand loadVideoCommand;
    private final FrameQuery frameQuery;

    private Video cachedVideo;
    private int index = 0;

    public MainViewController(
            LoadVideoCommand loadVideoCommand,
            FrameQuery frameQuery,
            TagsQuery tagsQuery) {
        this.loadVideoCommand = loadVideoCommand;
        this.frameQuery = frameQuery;
        this.tagsQuery = tagsQuery;
    }

    @FXML
    public void initialize(){
        dropLabel.setText(Dictionary.get("main.dropHere"));

        //drag and drop
        framePane.setOnDragOver(this::handleDragOver);
        framePane.setOnDragDropped(this::handleDragDropped);

        //set cell factories for the table
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setText(Dictionary.get("name"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueColumn.setText(Dictionary.get("value"));
    }

    //region [Drag and Drop]

    //drag event
    private void handleDragOver(DragEvent event) {
        if(event.getDragboard().hasFiles())
            event.acceptTransferModes(TransferMode.COPY);
        event.consume();
    }

    //drop event
    private void handleDragDropped(DragEvent event){
        Dragboard db = event.getDragboard();
        var success = false;

        if(db.hasFiles()){
            success = true;
            var file = db.getFiles().getFirst();

            try {
                cachedVideo = loadVideoCommand.loadVideo(file.getPath());
                openVideo();
            }catch (Exception e){
                logger.error(e.getMessage(),e);
            }
        }

        event.setDropCompleted(success);
        event.consume();
    }

    //endregion

    private void openVideo(){
        index = 0;
        displayCurrentData();

    }

    private void displayCurrentData() {
        if(!dropLabel.getText().isBlank())
            dropLabel.setText("");

        displayCurrentFrame();
        displayCurrentInfo();
        displayCurrentTags();
    }

    //region [Display loaders]

    private void displayCurrentFrame() {
        try {
            var frameBytes = frameQuery.getVideoFrame(cachedVideo, index);
            Image fxImage = new Image(new ByteArrayInputStream(frameBytes));

            if(fxImage.isError())
                throw new IOException("JavaFX failed to decode frame image for index " + index);

            frameView.setImage(fxImage);

            frameView.setPreserveRatio(true);
            frameView.setSmooth(true);
            frameView.setManaged(true);
            frameView.setPickOnBounds(true);
            frameView.fitWidthProperty().bind(framePane.widthProperty());
            frameView.fitHeightProperty().bind(framePane.heightProperty());
        } catch (IOException | InterruptedException e) {
            logger.error(e.getMessage(),e);
        }
    }

    private void displayCurrentInfo() {
        statusLabel.setText(String.format(
                Dictionary.get("main.fileInfo"), //TODO: change to adapter
                cachedVideo != null ?  index + 1 : 0,
                cachedVideo != null ? cachedVideo.getMetadata().totalFrames() : 0,
                cachedVideo != null ? cachedVideo.getMetadata().frameRate() : 0f
        ));
    }

    private void displayCurrentTags() {
        var currentTags = tagsQuery.getTagsOnVideoFrame(cachedVideo, index);
        tableView.getItems().clear();

        if (!currentTags.isEmpty())
            tableView.getItems().addAll(currentTags.stream().map(MainViewTagTableEntry::new).toList());
    }

    //endregion

    @FXML
    protected void onAdd() {

    }

    @FXML
    protected void onSettings() {

    }

    @FXML
    protected void onVideoList() {

    }

    @FXML
    protected void onExport() {

    }

    @FXML
    protected void onChart(){

    }

    @FXML
    protected void onManager() {

    }

    @FXML
    protected void onNotes() {

    }

    @FXML
    protected void onJumpToFrame() {

    }
}
