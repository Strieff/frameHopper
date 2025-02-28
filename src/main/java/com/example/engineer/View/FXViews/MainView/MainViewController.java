package com.example.engineer.View.FXViews.MainView;

import com.example.engineer.View.Elements.FXElementsProviders.FXDialogProvider;
import com.example.engineer.View.Elements.FXElementsProviders.FXIconLoader;
import com.example.engineer.View.Elements.FXElementsProviders.FXMLViewLoader;
import com.example.engineer.View.Elements.Language.Dictionary;
import com.example.engineer.View.Elements.Language.DictionaryCreator;
import com.example.engineer.View.Elements.Language.LanguageChangeListener;
import com.example.engineer.View.Elements.DataManagers.OpenViewsInformationContainer;
import com.example.engineer.View.Elements.Language.LanguageManager;
import com.example.engineer.View.Elements.UpdateTableEvent.UpdateTableEventDispatcher;
import com.example.engineer.View.Elements.UpdateTableEvent.UpdateTableListener;
import com.example.engineer.View.FXViews.TagManager.TagManagerController;
import com.example.engineer.View.FXViews.VideoDetails.VideoManagementDetailsController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.tika.Tika;
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
    private ImageView addButtonIcon, settingsButtonIcon, exportButtonIcon,chartButtonIcon;
    @FXML
    private BorderPane mainView;
    @FXML
    private Button jumpButton;

    @Autowired
    MainViewService viewService;
    @Autowired
    OpenViewsInformationContainer openViews;

    private final Map<KeyCombination,Runnable> keyActions = new HashMap<>();

    @FXML
    public void initialize(){
        UpdateTableEventDispatcher.register(this);
        LanguageManager.register(this);

        dropLabel.setOnDragOver(this::handleDragOver);
        dropLabel.setOnDragDropped(this::handleDragDropped);
        dropLabel.setText(Dictionary.get("main.dropHere"));

        // Map each KeyCombination to an action
        keyActions.put(new KeyCodeCombination(KeyCode.COMMA), this::onCommaPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.PERIOD), this::onPeriodPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.M, KeyCombination.SHIFT_DOWN), this::onShiftMPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN), this::onShiftSPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.E, KeyCombination.SHIFT_DOWN), this::onShiftEPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.L, KeyCombination.SHIFT_DOWN), this::onShiftLPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.D, KeyCombination.SHIFT_DOWN), this::onShiftDPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.C, KeyCombination.SHIFT_DOWN), this::onShiftCPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN), this::onCtrlVPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN), this::onCtrlXPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN), this::onCtrlYPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN), this::onCtrlZPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.Q, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), this::onAltShiftQPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), this::onAltShiftRPressed);

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

        Platform.runLater(() -> {
            var stage = (Stage) mainView.getScene().getWindow();
            stage.setOnCloseRequest(e -> {
                viewService.close();
                System.exit(0);
            });
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

    //DICTIONARY UPDATES
    private void onAltShiftRPressed() {
        DictionaryCreator.reload();
    }

    private void onAltShiftQPressed() {
        DictionaryCreator.create();
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
            else
                FXDialogProvider.errorDialog(Dictionary.get("open.tm"));
        }else
            FXDialogProvider.errorDialog(Dictionary.get("error.main.not-open"));
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
        else
            FXDialogProvider.errorDialog(Dictionary.get("open.settings"));
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
        else
            FXDialogProvider.errorDialog(Dictionary.get("open.export"));
    }

    @FXML
    protected void onChart(){
        openCharts();
    }

    private void onShiftCPressed(){
        openCharts();
    }

    private void openCharts(){
        if(!openViews.getCharts())
            try{
                var loader = FXMLViewLoader.getView("ChartsViewModel");

                Parent root = loader.load();
                var chartsScene = new Scene(root);

                var secondaryStage = new Stage();
                secondaryStage.setScene(chartsScene);
                secondaryStage.setTitle("TEST");

                secondaryStage.initOwner(tableView.getScene().getWindow());
                secondaryStage.show();
                openViews.openCharts();
            }catch (Exception e){
                FXDialogProvider.errorDialog(e.getMessage());
                e.printStackTrace();
            }
        else
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
            //get file
            var file = db.getFiles().getFirst();

            try {
                if(isValidFile(file))
                    prepareVideo(file);
                else
                    throw new Exception();
            }catch (Exception e){
                FXDialogProvider.errorDialog(Dictionary.get("error.drag"));
            }
        }

        event.setDropCompleted(success);
        event.consume();
    }

    private boolean isValidFile(File file) throws Exception{
        if(file.getName().endsWith(".gif"))
            return true;

        if(new Tika().detect(file).equals("video/"))
            return true;

        var extensions = new String[]{
                "*.gif","*.webm","*.mkv","*.flv","*.vob",
                "*.ogv","*.ogg","*.rrc","*.gifv","*.mng",
                "*.mov","*.avi","*.qt","*.wmv","*.yuv",
                "*.rm","*.asf","*.amv","*.mp4","*.m4p",
                "*.m4v","*.mpg","*.mp2","*.mpeg","*.mpe",
                "*.mpv","*.m4v","*.svi","*.3gp","*.3g2",
                "*.mxf","*.roq","*.nsv","*.flv","*.f4v",
                "*.f4p","*.f4a","*.f4b","*.mod"
        };

        for(String extension : extensions)
            if(file.getName().endsWith(extension))
                return true;

        return false;
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
            FXDialogProvider.errorDialog(Dictionary.get("error.main.frame.invalid"));
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

    //OPEN VIDEO LIST
    private void onShiftLPressed() {
        if(!openViews.getVideoList())
            try{
                var loader = FXMLViewLoader.getView("VideoManagementListViewModel");

                //load scene
                Parent root = loader.load();
                var videoListScene = new Scene(root);

                //new stage
                var secondaryStage = new Stage();
                secondaryStage.setScene(videoListScene);
                secondaryStage.setTitle("Video Management");

                //make window modal
                secondaryStage.initOwner(mainView.getScene().getWindow());
                secondaryStage.show();
                openViews.openVideoList();
            }catch(Exception e){
                FXDialogProvider.errorDialog(e.getMessage());
            }
        System.out.println("Shift + L pressed!");
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
                    throw new Exception(Dictionary.get("error.main.not-open"));

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
           FXDialogProvider.errorDialog(Dictionary.get("error.main.not-open"));
    }

    //PASTE RECENT
    private void onCtrlVPressed() {
        if(viewService.isOpen())
            viewService.pasteRecent();
        System.out.println("Ctrl + V pressed!");
    }

    //REMOVE RECENT
    private void onCtrlXPressed() {
        if(viewService.isOpen())
            viewService.removeRecent();
        System.out.println("Ctrl + X pressed!");
    }

    //REDO ACTION
    private void onCtrlYPressed() {
        if(viewService.isOpen())
            viewService.redo();
        System.out.println("Ctrl + Y pressed!");
    }

    //UNDO ACTION
    private void onCtrlZPressed() {
        if(viewService.isOpen())
            viewService.undo();
        System.out.println("Ctrl + Z pressed!");
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
}
