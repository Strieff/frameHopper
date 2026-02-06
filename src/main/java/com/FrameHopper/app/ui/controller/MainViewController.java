package com.FrameHopper.app.ui.controller;

import com.FrameHopper.app.View.Elements.FXElementsProviders.FXIconLoader;
import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.View.Elements.Language.DictionaryCreator;
import com.FrameHopper.app.boundry.dto.FrameDTO;
import com.FrameHopper.app.boundry.dto.TagDTO;
import com.FrameHopper.app.boundry.dto.VideoDTO;
import com.FrameHopper.app.core.ports.in.FrameBytesQuery;
import com.FrameHopper.app.core.ports.in.frame.FrameQuery;
import com.FrameHopper.app.core.ports.in.video.LoadVideoCommand;
import com.FrameHopper.app.ui.FXMLViewLoader;
import com.FrameHopper.app.ui.eventing.FrameUpdatedEventDispatcher;
import com.FrameHopper.app.ui.eventing.FrameUpdatedListener;
import com.FrameHopper.app.ui.eventing.TagUpdatedEventDispatcher;
import com.FrameHopper.app.ui.eventing.TagUpdatedEventListener;
import com.FrameHopper.app.ui.ve.MainViewTagTableEntry;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//TODO: move to dictionary adapter
@Component
@Scope("prototype")
public class MainViewController implements FrameUpdatedListener, TagUpdatedEventListener {
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
    private final FrameBytesQuery frameBytesQuery;

    private final Map<Integer, FrameDTO> cachedTags = new HashMap<>();
    private VideoDTO cachedVideo;
    private int index = 0;

    public MainViewController(
            LoadVideoCommand loadVideoCommand,
            FrameBytesQuery frameBytesQuery,
            FrameQuery frameQuery) {
        this.loadVideoCommand = loadVideoCommand;
        this.frameBytesQuery = frameBytesQuery;
        this.frameQuery = frameQuery;

        FrameUpdatedEventDispatcher.register(this);
        TagUpdatedEventDispatcher.register(this);
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

        //set up button icons
        addButtonIcon.setImage(FXIconLoader.getLargeIcon("plus.png"));
        settingsButtonIcon.setImage(FXIconLoader.getLargeIcon("settings.png"));
        chartButtonIcon.setImage(FXIconLoader.getLargeIcon("chart.png"));
        exportButtonIcon.setImage(FXIconLoader.getLargeIcon("export.png"));
        tagManagerIcon.setImage(FXIconLoader.getLargeIcon("tag.png"));
        notesButtonIcon.setImage(FXIconLoader.getLargeIcon("notes.png"));
        videoListButtonIcon.setImage(FXIconLoader.getLargeIcon("video-player.png"));

        //jump section
        jumpButton.setText(Dictionary.get("main.jump.button"));
        jumpButton.setOnAction(e -> jumpToFrame());
        frameInput.setPromptText(Dictionary.get("main.jump.hint"));

        //keybinds
        keyActions.put(new KeyCodeCombination(KeyCode.COMMA), this::moveLeft);
        keyActions.put(new KeyCodeCombination(KeyCode.PERIOD), this::moveRight);
        keyActions.put(new KeyCodeCombination(KeyCode.M, KeyCombination.SHIFT_DOWN), this::onAdd);
        keyActions.put(new KeyCodeCombination(KeyCode.F, KeyCombination.SHIFT_DOWN), this::onManager);
        keyActions.put(new KeyCodeCombination(KeyCode.T, KeyCombination.SHIFT_DOWN), this::onManager);
        keyActions.put(new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN), this::onSettings);
        keyActions.put(new KeyCodeCombination(KeyCode.E, KeyCombination.SHIFT_DOWN), this::onExport);
        keyActions.put(new KeyCodeCombination(KeyCode.L, KeyCombination.SHIFT_DOWN), this::onVideoList);
        //keyActions.put(new KeyCodeCombination(KeyCode.D, KeyCombination.SHIFT_DOWN), this::onShiftDPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.C, KeyCombination.SHIFT_DOWN), this::onChart);
        keyActions.put(new KeyCodeCombination(KeyCode.N, KeyCombination.SHIFT_DOWN), this::onNotes);
        //keyActions.put(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN), this::pasteRecent);
        //keyActions.put(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN), this::removeRecent);
        //keyActions.put(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN), this::redoAction);
        //keyActions.put(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN), this::undoAction);
        keyActions.put(new KeyCodeCombination(KeyCode.Q, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), DictionaryCreator::reload);//TODO: move to adapter
        keyActions.put(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), DictionaryCreator::create);

        //add key binds
        mainView.addEventFilter(KeyEvent.KEY_PRESSED,this::handleKeyPressed);

        Platform.runLater(() -> {
            var stage = (Stage) mainView.getScene().getWindow();
            stage.setOnCloseRequest(e -> System.exit(0));
            mainView.requestFocus();
        });
    }

    //handle key binds
    private void handleKeyPressed(KeyEvent event){
        keyActions.keySet().stream()
                .filter(k -> k.match(event))
                .findFirst()
                .ifPresent(k -> keyActions.get(k).run());
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
        cacheTagData();
        displayCurrentData();
    }

    private void cacheTagData() {
        cachedTags.clear();
        var allFramesOnVideo = frameQuery.getAllFramesOnVideo(cachedVideo);

        allFramesOnVideo.forEach(f -> cachedTags.put(f.frameNumber(), f));
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
            var frameBytes = frameBytesQuery.getVideoFrame(cachedVideo, index);
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
                cachedVideo != null ? cachedVideo.metadata().totalFrames() : 0,
                cachedVideo != null ? cachedVideo.metadata().frameRate() : 0f
        ));
    }

    private void displayCurrentTags() {
        if(!cachedTags.containsKey(index)) {
            tableView.getItems().clear();
            return;
        }

        var currentTags = cachedTags.get(index).tags();
        tableView.getItems().clear();

        if (!currentTags.isEmpty())
            tableView.getItems().addAll(currentTags.stream().map(MainViewTagTableEntry::new).toList());
    }

    //endregion

    //region FXML

    @FXML
    protected void onAdd() {
        //TODO: check if already open

        var loader = FXMLViewLoader.getView(
                "FrameTagManagerViewModel",
                "Frame Tag manager",
                mainView
        );

        FrameTagManagerController controller = loader.getController();

        var frame = cachedTags.getOrDefault(index, new FrameDTO(-1, index, cachedVideo, new ArrayList<>()));

        controller.init(frame);
    }

    @FXML
    protected void onSettings() {
        FXMLViewLoader.getView(
                "SettingsViewModel",
                "Settings",
                mainView
        );
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
        //TODO: check if already open
        FXMLViewLoader.getView(
                "TagManagerViewModel",
                "Settings",
                mainView
        );
    }

    @FXML
    protected void onNotes() {

    }

    @FXML
    protected void onJumpToFrame() {

    }

    //endregion

    //region Movement

    private void moveRight() {
        if(cachedVideo == null) return;

        if(index + 1 > cachedVideo.metadata().totalFrames() - 1) return;

        index++;
        displayCurrentData();
    }

    private void moveLeft() {
        if(cachedVideo == null) return;

        if(index - 1 < 0) return;

        index--;
        displayCurrentData();
    }

    private void jumpToFrame() {
        if(cachedVideo == null) return;

        int frame;
        try{
            frame = Integer.parseInt(frameInput.getText());
        } catch (NumberFormatException e){
            return;
        }

        if(frame - 1 < 0 || frame > cachedVideo.metadata().totalFrames()) return;

        index = frame;

        displayCurrentData();
    }

    //endregion

    @Override
    public void onFrameUpdate(int index, FrameDTO frame) {
        if(frame == null)
            cachedTags.remove(index);
        else
            cachedTags.put(index, frame);

        displayCurrentTags();
    }

    //region [TAG LIST UPDATE]

    @Async
    @Override
    public void onTagUpdated(TagDTO tagDTO) {
        cacheTagData();
        displayCurrentTags();
    }

    @Override
    public void onTagCreated(TagDTO tagDTO) {
        //NOT NEEDED
    }

    @Async
    @Override
    public void onTagDeleted(TagDTO tagDTO) {
        cacheTagData();
        displayCurrentTags();
    }

    //endregion
}
