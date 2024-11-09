package com.example.engineer.View.FXViews.MainView;

import com.example.engineer.View.Elements.FXDialogProvider;
import com.example.engineer.View.Elements.FXIconLoader;
import com.example.engineer.View.Elements.FXMLViewLoader;
import com.example.engineer.View.Elements.Language.LanguageChangeListener;
import com.example.engineer.View.Elements.OpenViewsInformationContainer;
import com.example.engineer.View.Elements.UpdateTableEvent.UpdateTableEventDispatcher;
import com.example.engineer.View.Elements.UpdateTableEvent.UpdateTableListener;
import com.example.engineer.View.FXViews.TagManager.TagManagerController;
import com.example.engineer.View.FXViews.VideoDetails.VideoManagementDetailsController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Component
@Scope("prototype")
public class MainViewController implements LanguageChangeListener, UpdateTableListener{
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
    private ImageView addButtonIcon, settingsButtonIcon, exportButtonIcon;
    @FXML
    private BorderPane mainView;

    @Autowired
    MainViewService viewService;
    @Autowired
    OpenViewsInformationContainer openViews;

    private final Map<KeyCombination,Runnable> keyActions = new HashMap<>();

    @FXML
    public void initialize(){
        UpdateTableEventDispatcher.register(this);

        dropLabel.setOnDragOver(this::handleDragOver);
        dropLabel.setOnDragDropped(this::handleDragDropped);

        // Map each KeyCombination to an action
        keyActions.put(new KeyCodeCombination(KeyCode.COMMA), this::onCommaPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.PERIOD), this::onPeriodPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.M, KeyCombination.SHIFT_DOWN), this::onShiftMPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN), this::onShiftSPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.E, KeyCombination.SHIFT_DOWN), this::onShiftEPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.L, KeyCombination.SHIFT_DOWN), this::onShiftLPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.D, KeyCombination.SHIFT_DOWN), this::onShiftDPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN), this::onCtrlVPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN), this::onCtrlXPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN), this::onCtrlYPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN), this::onCtrlZPressed);

        //add key binds
        mainView.addEventFilter(KeyEvent.KEY_PRESSED,this::handleKeyPressed);

        //set cell factories for the table
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

        //set up button icons
        addButtonIcon.setImage(FXIconLoader.getLargeIcon("plus.png"));
        settingsButtonIcon.setImage(FXIconLoader.getLargeIcon("settings.png"));
        exportButtonIcon.setImage(FXIconLoader.getLargeIcon("export.png"));

        Platform.runLater(() -> {
            var stage = (Stage) mainView.getScene().getWindow();
            stage.setOnCloseRequest(e -> viewService.close());
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

    //OPEN TAG MANAGER
    @FXML
    protected void onAdd() {
        openTagManager();
        System.out.println("Add button clicked");
    }

    private void onShiftMPressed() {
        openTagManager();
        System.out.println("Shift + M pressed! Opening tag manager");
    }

    private void openTagManager(){
        if(viewService.isOpen()) {
            if (!openViews.getTagManager())
                try {
                    var loader = FXMLViewLoader.getView("TagManagerViewModel");

                    //load scene
                    Parent root = loader.load();
                    var tagManagerScene = new Scene(root);

                    //get controller
                    TagManagerController tagManagerController = loader.getController();
                    tagManagerController.init(viewService.getCurrentIndex());

                    //new stage
                    var secondaryStage = new Stage();
                    secondaryStage.setScene(tagManagerScene);
                    secondaryStage.setTitle("Tag manager");

                    //make it a modal window
                    secondaryStage.initOwner(mainView.getScene().getWindow());
                    secondaryStage.show();
                    openViews.openTagManager();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
        }else
            FXDialogProvider.errorDialog("Tag manager error","No file is open!");
    }

    //OPEN SETTINGS
    @FXML
    protected void onSettings() {
        openSettings();
        System.out.println("Settings button clicked");
    }

    private void onShiftSPressed() {
        openSettings();
        // Your code here
    }

    private void openSettings(){
        if(!openViews.getSettings())
            try{
                var loader = FXMLViewLoader.getView("SettingsViewModel");

                //load scene
                Parent root = loader.load();
                var settingsScene = new Scene(root);

                //new stage
                var secondaryStage = new Stage();
                secondaryStage.setScene(settingsScene);
                secondaryStage.setTitle("Settings");

                //make it a modal window
                secondaryStage.initOwner(mainView.getScene().getWindow());
                secondaryStage.show();
                openViews.openSettings();
            }catch (Exception e){
                e.printStackTrace();
            }
    }

    //OPEN EXPORT
    @FXML
    protected void onExport() {
        openExport();
        System.out.println("Export button clicked");
    }

    private void onShiftEPressed() {
        openExport();
        System.out.println("Shift + E pressed!");
    }

    private void openExport(){
        if(!openViews.getExport())
            try {
                var loader = FXMLViewLoader.getView("ExportViewModel");

                //load scene
                Parent root = loader.load();
                var exportScene = new Scene(root);

                //new stage
                var secondaryStage = new Stage();
                secondaryStage.setScene(exportScene);
                secondaryStage.setTitle("Export");

                //make it a modal window
                secondaryStage.initOwner(mainView.getScene().getWindow());
                secondaryStage.show();
                openViews.openExport();
            }catch (Exception e){
                e.printStackTrace();
            }
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
            //get file
            var file = db.getFiles().get(0);

            prepareVideo(file);
        }

        event.setDropCompleted(success);
        event.consume();
    }

    public void openRecent(String path){
        prepareVideo(new File(path));
    }

    public void prepareVideo(File file){
        //get cache
        var cache = viewService.setCache(file);
        //get DB video
        var video = viewService.getVideo(file);

        //prepare necessary items
        viewService.prepareVideo(
                cache,
                video,
                file,
                dropLabel
        );

        //display first frame and hide text
        dropLabel.setGraphic(viewService.displayCurrentFrame());
        dropLabel.setText("");

        //display tags
        tableView.setItems(viewService.displayCurrentTags());

        //display data
        statusLabel.setText(viewService.displayCurrentInfo());
    }

    //jump frames
    @FXML
    protected void onJumpToFrame() {
        if(isValidNumber()){
            var toJump = Integer.parseInt(frameInput.getText());

            dropLabel.setGraphic(viewService.jump(toJump));
            tableView.setItems(viewService.displayCurrentTags());
            statusLabel.setText(viewService.displayCurrentInfo());
            System.out.println("Jump to frame: " + toJump);
        }else
            FXDialogProvider.errorDialog("Invalid frame");
    }

    //check if number is valid
    private boolean isValidNumber(){
        int num;

        try {
            num = Integer.parseInt(frameInput.getText());
        }catch (Exception e){
            return false;
        }

        return num > 0;
    }

    //move right
    private void onCommaPressed() {
        dropLabel.setGraphic(viewService.moveLeft());
        tableView.setItems(viewService.displayCurrentTags());
        statusLabel.setText(viewService.displayCurrentInfo());
        System.out.println("Comma key pressed!");
    }

    //move left
    private void onPeriodPressed() {
        dropLabel.setGraphic(viewService.moveRight());
        tableView.setItems(viewService.displayCurrentTags());
        statusLabel.setText(viewService.displayCurrentInfo());
        System.out.println("Period key pressed!");
    }

    //TODO
    private void onShiftLPressed() {
        System.out.println("Shift + L pressed!");
        // Your code here
    }

    //OPEN VIDEO DETAILS
    private void onShiftDPressed() {
       if(viewService.isOpen())
            try{
                var loader = FXMLViewLoader.getView("VideoManagementDetailsViewModel");

                //load scene
                Parent root = loader.load();
                var videoDetailsScene = new Scene(root);

                //get controller
                VideoManagementDetailsController videoController = loader.getController();
                var vid = viewService.getCurrentVideo();
                if(vid != null)
                    videoController.init(vid);
                else
                    throw new Exception("No video is open");

                //new stage
                var secondaryStage = new Stage();
                secondaryStage.setScene(videoDetailsScene);
                secondaryStage.setTitle("Video Details");

                //make it a modal window
                secondaryStage.initOwner(mainView.getScene().getWindow());
                secondaryStage.show();
            }catch (Exception e){
                FXDialogProvider.errorDialog(e.getMessage());
            }
       else
           FXDialogProvider.errorDialog("No video is open");
    }

    //TODO
    private void onCtrlVPressed() {
        System.out.println("Ctrl + V pressed!");
        // Your code here
    }

    //TODO
    private void onCtrlXPressed() {
        System.out.println("Ctrl + X pressed!");
        // Your code here
    }

    //TODO
    private void onCtrlYPressed() {
        System.out.println("Ctrl + Y pressed!");
        // Your code here
    }

    //TODO
    private void onCtrlZPressed() {
        System.out.println("Ctrl + Z pressed!");
        // Your code here
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

    }
}
