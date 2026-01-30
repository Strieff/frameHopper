package com.FrameHopper.app.View.FXViews.MainView;

import com.FrameHopper.app.View.Elements.DataManagers.ViewContainer.ViewFlag;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXDialogProvider;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXIconLoader;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXMLViewLoader;
import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.View.Elements.Language.DictionaryCreator;
import com.FrameHopper.app.View.Elements.Language.LanguageChangeListener;
import com.FrameHopper.app.View.Elements.DataManagers.ViewContainer.OpenViewsInformationContainer;
import com.FrameHopper.app.View.Elements.Language.LanguageManager;
import com.FrameHopper.app.View.Elements.OpenVideo.OpenVideoEventDispatcher;
import com.FrameHopper.app.View.Elements.OpenVideo.OpenVideoListener;
import com.FrameHopper.app.View.Elements.UpdateTableEvent.UpdateTableEventDispatcher;
import com.FrameHopper.app.View.Elements.UpdateTableEvent.UpdateTableListener;
import com.FrameHopper.app.View.FXViews.FrameTagManager.FrameTagManagerController;
import com.FrameHopper.app.View.FXViews.VideoDetails.VideoManagementDetailsController;
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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Component
@Scope("prototype")
public class MainViewController implements LanguageChangeListener, UpdateTableListener, OpenVideoListener {
    @FXML
    private TextField frameInput;
    @FXML
    private Label dropLabel, statusLabel;
    @FXML
    private TableView<TableEntry> tableView;
    @FXML
    private TableColumn<TableEntry,String> nameColumn;
    @FXML
    private TableColumn<TableEntry,Double> valueColumn;
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

    private final MainViewService viewService;
    private final OpenViewsInformationContainer viewContainer;

    private final Map<KeyCombination,Runnable> keyActions = new HashMap<>();

    public MainViewController(MainViewService viewService, OpenViewsInformationContainer viewContainer) {
        this.viewService = viewService;
        this.viewContainer = viewContainer;

        UpdateTableEventDispatcher.register(this);
        LanguageManager.register(this);
        OpenVideoEventDispatcher.register(this);
    }

    @FXML
    public void initialize(){
        framePane.setOnDragOver(this::handleDragOver);
        framePane.setOnDragDropped(this::handleDragDropped);
        dropLabel.setText(Dictionary.get("main.dropHere"));

        // Map each KeyCombination to an action
        keyActions.put(new KeyCodeCombination(KeyCode.COMMA), this::moveRight);
        keyActions.put(new KeyCodeCombination(KeyCode.PERIOD), this::moveLeft);
        keyActions.put(new KeyCodeCombination(KeyCode.M, KeyCombination.SHIFT_DOWN), this::onAdd);
        keyActions.put(new KeyCodeCombination(KeyCode.F, KeyCombination.SHIFT_DOWN), this::onManager);
        keyActions.put(new KeyCodeCombination(KeyCode.T, KeyCombination.SHIFT_DOWN), this::onManager);
        keyActions.put(new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN), this::onSettings);
        keyActions.put(new KeyCodeCombination(KeyCode.E, KeyCombination.SHIFT_DOWN), this::onExport);
        keyActions.put(new KeyCodeCombination(KeyCode.L, KeyCombination.SHIFT_DOWN), this::onVideoList);
        keyActions.put(new KeyCodeCombination(KeyCode.D, KeyCombination.SHIFT_DOWN), this::onShiftDPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.C, KeyCombination.SHIFT_DOWN), this::onChart);
        keyActions.put(new KeyCodeCombination(KeyCode.N, KeyCombination.SHIFT_DOWN), this::onNotes);
        keyActions.put(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN), this::pasteRecent);
        keyActions.put(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN), this::removeRecent);
        keyActions.put(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN), this::redoAction);
        keyActions.put(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN), this::undoAction);
        keyActions.put(new KeyCodeCombination(KeyCode.Q, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), DictionaryCreator::reload);
        keyActions.put(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), DictionaryCreator::create);

        //jump section
        jumpButton.setText(Dictionary.get("main.jump.button"));
        frameInput.setPromptText(Dictionary.get("main.jump.hint"));

        //info label
        statusLabel.setText(viewService.displayCurrentInfo());

        //table placeholder
        tableView.setPlaceholder(new Label(Dictionary.get("placeholder.codes")));

        //add key binds
        mainView.addEventFilter(KeyEvent.KEY_PRESSED,this::handleKeyPressed);

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


        Platform.runLater(() -> {
            var stage = (Stage) mainView.getScene().getWindow();
            stage.setOnCloseRequest(e -> System.exit(0));
            mainView.requestFocus();
        });
    }

    //HANDLE KEY BINDS
    private void handleKeyPressed(KeyEvent event){
        keyActions.keySet().stream()
                .filter(k -> k.match(event))
                .findFirst()
                .ifPresent(k -> keyActions.get(k).run());
    }

    //OPEN FRAME TAG MANAGER
    @FXML
    protected void onAdd() {
        if(viewService.isOpen()) {
            if (viewContainer.isClosed(ViewFlag.FRAME_TAG_MANAGER)) {
                viewContainer.open(ViewFlag.FRAME_TAG_MANAGER);

                var loader = FXMLViewLoader.getView(
                        "FrameTagManagerViewModel",
                        "Frame Tag manager",
                        mainView
                );

                //get controller
                FrameTagManagerController frameTagManagerController = loader.getController();
                frameTagManagerController.init(viewService.getCurrentIndex());
            }
            else
                FXDialogProvider.errorDialog(Dictionary.get("open.tm"));
        } else
            FXDialogProvider.errorDialog(Dictionary.get("error.main.not-open"));
    }

    //OPEN SETTINGS
    @FXML
    protected void onSettings() {
        if(viewContainer.isClosed(ViewFlag.SETTINGS)) {
            FXMLViewLoader.getView(
                    "SettingsViewModel",
                    "Settings",
                    mainView
            );
            viewContainer.open(ViewFlag.SETTINGS);
        } else
            FXDialogProvider.errorDialog(Dictionary.get("open.settings"));
    }

    //OPEN VIDEO LIST
    @FXML
    protected void onVideoList() {
        if(viewContainer.isClosed(ViewFlag.VIDEO_LIST)) {
            FXMLViewLoader.getView(
                    "VideoManagementListViewModel",
                    "Video Management",
                    mainView
            );
            viewContainer.open(ViewFlag.VIDEO_LIST);
        } else
            FXDialogProvider.errorDialog(Dictionary.get("open.video-list"));
    }

    //OPEN EXPORT
    @FXML
    protected void onExport() {
        if(viewContainer.isClosed(ViewFlag.EXPORT)) {
            FXMLViewLoader.getView(
                    "ExportViewModel",
                    "Export",
                    mainView
            );
            viewContainer.open(ViewFlag.EXPORT);
        } else
            FXDialogProvider.errorDialog(Dictionary.get("open.export"));
    }

    @FXML
    protected void onChart(){
        if(viewContainer.isClosed(ViewFlag.CHARTS)) {
            FXMLViewLoader.getView(
                    "ChartsViewModel",
                    "Charts",
                    mainView
            );
            viewContainer.open(ViewFlag.CHARTS);
        } else
            FXDialogProvider.errorDialog(Dictionary.get("open.chart"));
    }

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
                if(viewService.isFfmpegAvailable())
                    prepareVideo(file);
                else
                    FXDialogProvider.errorDialog(Dictionary.get("ffmpeg.not-available"));
            }catch (Exception e){
                FXDialogProvider.errorDialog(Dictionary.get("error.drag"));
                e.printStackTrace();
            }
        }

        event.setDropCompleted(success);
        event.consume();
    }

    public void prepareVideo(File file) {
        //get DB video
        var video = viewService.getVideo(file);

        //prepare necessary items
        viewService.prepareVideo(
                video,
                file,
                dropLabel
        );

        prepareElements();
    }

    private void prepareElements() {
        //display first frame and hide text
        updateFrameDisplay(viewService.displayCurrentFrame());

        dropLabel.setText("");

        //display tags
        tableView.setItems(viewService.displayCurrentTags());

        //display data
        statusLabel.setText(viewService.displayCurrentInfo());
    }

    //jump frames
    @FXML
    protected void onJumpToFrame() {
        var jumpInput = frameInput.getText();
        if(viewService.isValidNumber(jumpInput)){
            var toJump = Integer.parseInt(jumpInput);

            updateFrameDisplay(viewService.jump(toJump));
            tableView.setItems(viewService.displayCurrentTags());
            statusLabel.setText(viewService.displayCurrentInfo());
            System.out.println("Jump to frame: " + toJump);
        }else
            FXDialogProvider.errorDialog(Dictionary.get("error.main.frame.invalid"));
    }

    //move right
    private void moveRight() {
        updateFrameDisplay(viewService.moveLeft());
        tableView.setItems(viewService.displayCurrentTags());
        statusLabel.setText(viewService.displayCurrentInfo());
    }

    //move left
    private void moveLeft() {
        updateFrameDisplay(viewService.moveRight());
        tableView.setItems(viewService.displayCurrentTags());
        statusLabel.setText(viewService.displayCurrentInfo());
    }

    //OPEN VIDEO DETAILS
    private void onShiftDPressed() {
       if(viewService.isOpen())
            try{
                var loader = FXMLViewLoader.getView(
                        "VideoManagementDetailsViewModel",
                        "Video Details",
                        mainView
                );

                //get controller
                VideoManagementDetailsController videoController = loader.getController();
                var vid = viewService.getCurrentVideo();
                if(vid != null)
                    videoController.init(vid);
                else
                    throw new Exception(Dictionary.get("error.main.not-open"));
            }catch (Exception e){
                e.printStackTrace();
                FXDialogProvider.errorDialog(e.getMessage());
            }
       else
           FXDialogProvider.errorDialog(Dictionary.get("error.main.not-open"));
    }

    //TAG MANAGER
    @FXML
    protected void onManager() {
        if(viewContainer.isClosed(ViewFlag.TAG_MANAGER)) {
            FXMLViewLoader.getView(
                    "TagManagerViewModel",
                    "Settings",
                    mainView
            );
            viewContainer.open(ViewFlag.TAG_MANAGER);
        }
        else
            FXDialogProvider.errorDialog(Dictionary.get("open.tag-manager"));
    }

    //NOTES
    @FXML
    protected void onNotes() {
        if(viewContainer.isClosed(ViewFlag.NOTES)) {
            FXMLViewLoader.getView(
                    "NotesViewModel",
                    "Notes",
                    mainView
            );
            viewContainer.open(ViewFlag.NOTES);
        } else
            FXDialogProvider.errorDialog(Dictionary.get("open.notes"));
        System.out.println("OPEN NOTES");
    }

    //PASTE RECENT
    private void pasteRecent() {
        if(viewService.isOpen())
            viewService.pasteRecent();
        System.out.println("Ctrl + V pressed!");
    }

    //REMOVE RECENT
    private void removeRecent() {
        if(viewService.isOpen())
            viewService.removeRecent();
        System.out.println("Ctrl + X pressed!");
    }

    //REDO ACTION
    private void redoAction() {
        if(viewService.isOpen())
            viewService.redo();
        System.out.println("Ctrl + Y pressed!");
    }

    //UNDO ACTION
    private void undoAction() {
        if(viewService.isOpen())
            viewService.undo();
        System.out.println("Ctrl + Z pressed!");
    }

    //update frame view
    public void updateFrameDisplay(Image frame) {
        frameView.setImage(frame);

        frameView.setPreserveRatio(true);
        frameView.setSmooth(true);

        frameView.setManaged(true);
        frameView.setPickOnBounds(true);

        frameView.fitWidthProperty().bind(framePane.widthProperty());
        frameView.fitHeightProperty().bind(framePane.heightProperty());
    }

    @Override
    public void updateTable(){
        Platform.runLater(() -> {
            if(viewService.isOpen())
                tableView.setItems(viewService.displayCurrentTags());
        });
    }

    @Override
    public void changeLanguage() {
        jumpButton.setText(Dictionary.get("main.jump.button"));
        frameInput.setPromptText(Dictionary.get("main.jump.hint"));
        statusLabel.setText(viewService.displayCurrentInfo());
        if(!viewService.isOpen())
            dropLabel.setText(Dictionary.get("main.dropHere"));
        nameColumn.setText(Dictionary.get("name"));
        valueColumn.setText(Dictionary.get("value"));
        tableView.setPlaceholder(new Label(Dictionary.get("placeholder.codes")));
    }

    @Override
    public void openVideo(int id) {
        //get DB video
        var video = viewService.getVideo(id);
        var file = new  File(video.getPath());

        //prepare necessary items
        viewService.prepareVideo(
                video,
                file,
                dropLabel
        );

        prepareElements();
    }
}
