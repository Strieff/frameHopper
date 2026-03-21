package com.FrameHopper.app.ui.controller;

import com.FrameHopper.app.View.Elements.FXElementsProviders.FXIconLoader;
import com.FrameHopper.app.boundry.dto.FrameDTO;
import com.FrameHopper.app.boundry.dto.TagDTO;
import com.FrameHopper.app.boundry.dto.VideoDTO;
import com.FrameHopper.app.core.ports.in.FrameBytesQuery;
import com.FrameHopper.app.core.ports.in.frame.FrameQuery;
import com.FrameHopper.app.core.ports.in.video.LoadVideoCommand;
import com.FrameHopper.app.core.ports.in.video.VideoQuery;
import com.FrameHopper.app.ui.UIFlag;
import com.FrameHopper.app.ui.UIManager;
import com.FrameHopper.app.ui.UiView;
import com.FrameHopper.app.ui.eventing.*;
import com.FrameHopper.app.ui.language.I18n;
import com.FrameHopper.app.ui.ve.MainViewTagTableEntry;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO: move to dictionary adapter
@Component
@Scope("prototype")
public class MainViewController extends UiView implements
        FrameUpdatedListener,
        TagUpdatedEventListener,
        TagDeletedEventListener,
        OpenVideoEventListener,
        DeleteVideoEventListener,
        VideoPathUpdatedListener
{
    private final VideoQuery videoQuery;
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

    private final Logger logger = LoggerFactory.getLogger(MainViewController.class);

    private final LoadVideoCommand loadVideoCommand;
    private final FrameQuery frameQuery;
    private final FrameBytesQuery frameBytesQuery;
    private final UIManager uiManager;

    private final Map<Integer, FrameDTO> cachedTags = new HashMap<>();

    private final ObjectProperty<VideoDTO> cachedVideoProperty = new SimpleObjectProperty<>(null);
    private final IntegerProperty indexProperty = new SimpleIntegerProperty(0);

    public MainViewController(
            LoadVideoCommand loadVideoCommand,
            FrameBytesQuery frameBytesQuery,
            FrameQuery frameQuery,
            VideoQuery videoQuery,
            UIManager uiManager
    ) {
        this.loadVideoCommand = loadVideoCommand;
        this.frameBytesQuery = frameBytesQuery;
        this.frameQuery = frameQuery;
        this.videoQuery = videoQuery;
        this.uiManager = uiManager;

        FrameUpdatedEventDispatcher.register(this);
        TagUpdatedEventDispatcher.register(this);
        TagDeletedEventDispatcher.register(this);
        OpenVideoEventDispatcher.register(this);
        DeleteVideoEventDispatcher.register(this);
    }

    @FXML
    public void initialize(){
        mainView.setOnMouseClicked(e -> mainView.requestFocus());

        bind(dropLabel, "main.drop-placeholder");

        //drag and drop
        framePane.setOnDragOver(this::handleDragOver);
        framePane.setOnDragDropped(this::handleDragDropped);

        //set cell factories for the table
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        bind(nameColumn, "main.table.name");
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        bind(valueColumn, "main.table.value");

        //set up button icons
        addButtonIcon.setImage(FXIconLoader.getLargeIcon("plus.png"));
        settingsButtonIcon.setImage(FXIconLoader.getLargeIcon("settings.png"));
        chartButtonIcon.setImage(FXIconLoader.getLargeIcon("chart.png"));
        exportButtonIcon.setImage(FXIconLoader.getLargeIcon("export.png"));
        tagManagerIcon.setImage(FXIconLoader.getLargeIcon("tag.png"));
        notesButtonIcon.setImage(FXIconLoader.getLargeIcon("notes.png"));
        videoListButtonIcon.setImage(FXIconLoader.getLargeIcon("video-player.png"));

        //jump section
        bind(jumpButton, "main.jump.button");
        bind(frameInput, "main.jump.prompt");

        //status label
        statusLabel.textProperty().bind(
                Bindings.createStringBinding(
                        () -> {
                            var video = cachedVideoProperty.get();

                            if(video == null)
                                return I18n.tr("main.video-info.no-video");

                            return I18n.tr(
                                    "main.video-info",
                                    indexProperty.get() + 1,
                                    video.metadata().totalFrames(),
                                    video.metadata().frameRate()
                            );
                        },
                        I18n.localeProperty(),
                        indexProperty,
                        cachedVideoProperty
                )
        );

        addKeybinds();

        Platform.runLater(() -> {
            var stage = (Stage) mainView.getScene().getWindow();
            stage.setOnCloseRequest(e -> System.exit(0));
            bind(stage, "main.stage");
            mainView.requestFocus();
        });
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
                cachedVideoProperty.set(loadVideoCommand.loadVideo(file.getPath()));
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
        indexProperty.set(0);
        cacheTagData();
        displayCurrentData();
    }

    private void cacheTagData() {
        cachedTags.clear();
        var allFramesOnVideo = frameQuery.getAllFramesOnVideo(cachedVideoProperty.get());

        allFramesOnVideo.forEach(f -> cachedTags.put(f.frameNumber(), f));
    }

    private void displayCurrentData() {
        if(!dropLabel.getText().isBlank())
            dropLabel.setVisible(false);

        displayCurrentFrame();
        displayCurrentTags();
    }

    //region [Display loaders]

    private void displayCurrentFrame() {
        try {
            var index = indexProperty.get();
            var frameBytes = frameBytesQuery.getVideoFrame(cachedVideoProperty.get(), index);
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

    private void displayCurrentTags() {
        var index = indexProperty.get();

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
        //TODO: error when open

        if(uiManager.isOpen(UIFlag.FRAME_TAG_MANAGER))
            return;

        var index = indexProperty.get();

        var loader = uiManager.open(UIFlag.FRAME_TAG_MANAGER, mainView);

        FrameTagManagerController controller = loader.getController();

        var frame = cachedTags.getOrDefault(index, new FrameDTO(-1, index, cachedVideoProperty.get(), new ArrayList<>()));

        controller.init(frame);
    }

    @FXML
    protected void onSettings() {
        //TODO: error when open
        if(uiManager.isOpen(UIFlag.SETTINGS))
            return;

        uiManager.open(UIFlag.SETTINGS, mainView);
    }

    @FXML
    protected void onVideoList() {
        //TODO: error when open
        if(uiManager.isOpen(UIFlag.VIDEO_LIST))
            return;

        uiManager.open(UIFlag.VIDEO_LIST, mainView);
    }

    @FXML
    protected void onExport() {
        //TODO: error when open
        if(uiManager.isOpen(UIFlag.EXPORT))
            return;

        uiManager.open(UIFlag.EXPORT, mainView);
    }

    @FXML
    protected void onChart(){
        //TODO: error when open
        if(uiManager.isOpen(UIFlag.CHARTS))
            return;

        uiManager.open(UIFlag.CHARTS, mainView);
    }

    @FXML
    protected void onManager() {
        //TODO: error when open
        if(uiManager.isOpen(UIFlag.TAG_MANAGER))
            return;

        uiManager.open(UIFlag.TAG_MANAGER, mainView);
    }

    @FXML
    protected void onNotes() {
        //TODO: error when open
        if(uiManager.isOpen(UIFlag.NOTES))
            return;

        uiManager.open(UIFlag.NOTES, mainView);
    }

    //endregion

    private void openVideoDetails(){
        var cachedVideo = cachedVideoProperty.get();

        if(cachedVideo == null) return;

        var loader = uiManager.open(UIFlag.VIDEO_DETAILS, mainView);

        VideoDetailsController controller = loader.getController();
        controller.init(cachedVideo);
    }

    //region Movement

    private void moveRight() {
        var cachedVideo = cachedVideoProperty.get();
        var index = indexProperty.get();

        if(cachedVideo == null) return;

        if(index + 1 > cachedVideo.metadata().totalFrames() - 1) return;

        indexProperty.set(++index);
        displayCurrentData();
    }

    private void moveLeft() {
        var cachedVideo = cachedVideoProperty.get();
        var index = indexProperty.get();

        if(cachedVideo == null) return;

        if(index - 1 < 0) return;

        indexProperty.set(--index);
        displayCurrentData();
    }

    @FXML
    protected void onJumpToFrame() {
        var cachedVideo = cachedVideoProperty.get();

        if(cachedVideo == null) return;

        int frame;
        try{
            frame = Integer.parseInt(frameInput.getText()) - 1;
        } catch (NumberFormatException e){
            return;
        }

        if(frame - 1 < 0 || frame > cachedVideo.metadata().totalFrames()) return;

        indexProperty.set(frame);

        displayCurrentData();
    }

    //endregion

    @Async
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
    public void onTagUpdated(@NotNull TagDTO tagDTO) {
        if(cachedVideoProperty.get() == null) return;
        cacheTagData();
        displayCurrentTags();
    }

    @Async
    @Override
    public void onTagUpdated(@NotNull List<TagDTO> tagsDTO) {
        if(cachedVideoProperty.get() == null) return;

        cacheTagData();
        displayCurrentTags();
    }

    @Async
    @Override
    public void onTagDeleted(@NotNull TagDTO tagDTO) {
        if(cachedVideoProperty.get() == null) return;

        cacheTagData();
        displayCurrentTags();
    }

    @Override
    public void onTagDeleted(@NotNull List<TagDTO> tags) {
        if(cachedVideoProperty.get() == null) return;

        cacheTagData();
        displayCurrentTags();
    }

    //endregion

    @Override
    public void openVideo(int id) {
        cachedVideoProperty.set(loadVideoCommand.loadVideo(id));
        openVideo();
    }

    @Override
    protected void addKeybinds() {
        //keybinds
        keyActions.put(new KeyCodeCombination(KeyCode.COMMA), this::moveLeft);
        keyActions.put(new KeyCodeCombination(KeyCode.PERIOD), this::moveRight);
        keyActions.put(new KeyCodeCombination(KeyCode.M, KeyCombination.SHIFT_DOWN), this::onAdd);
        keyActions.put(new KeyCodeCombination(KeyCode.F, KeyCombination.SHIFT_DOWN), this::onManager);
        keyActions.put(new KeyCodeCombination(KeyCode.T, KeyCombination.SHIFT_DOWN), this::onManager);
        keyActions.put(new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN), this::onSettings);
        keyActions.put(new KeyCodeCombination(KeyCode.E, KeyCombination.SHIFT_DOWN), this::onExport);
        keyActions.put(new KeyCodeCombination(KeyCode.L, KeyCombination.SHIFT_DOWN), this::onVideoList);
        keyActions.put(new KeyCodeCombination(KeyCode.C, KeyCombination.SHIFT_DOWN), this::onChart);
        keyActions.put(new KeyCodeCombination(KeyCode.N, KeyCombination.SHIFT_DOWN), this::onNotes);
        keyActions.put(new KeyCodeCombination(KeyCode.D, KeyCombination.SHIFT_DOWN), this::openVideoDetails);
        //keyActions.put(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN), this::pasteRecent);
        //keyActions.put(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN), this::removeRecent);
        //keyActions.put(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN), this::redoAction);
        //keyActions.put(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN), this::undoAction);
        //keyActions.put(new KeyCodeCombination(KeyCode.Q, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), DictionaryCreator::reload);//TODO: move to adapter
        //keyActions.put(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), DictionaryCreator::create);

        //add key binds
        addEventFilter(mainView);
    }

    @Override
    protected void close() {

    }

    @Async
    @Override
    public void onDeleteVideo(@NotNull VideoDTO videoDTO) {
        var cachedVideo = cachedVideoProperty.get();

        if(cachedVideo == null) return;

        if(cachedVideo.equals(videoDTO)) return;

        cachedVideoProperty.set(null);
        indexProperty.set(0);

        cachedTags.clear();
        tableView.getItems().clear();

        frameView.setImage(null);
        dropLabel.setVisible(true);
    }

    @Override
    public void onVideoPathUpdated(@NotNull VideoDTO video) {
        if(cachedVideoProperty.get().equals(video))
            cachedVideoProperty.set(video);
    }
}
