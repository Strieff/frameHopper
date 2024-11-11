package com.example.engineer.View.FXViews.Settings;

import com.example.engineer.View.Elements.DataManagers.OpenViewsInformationContainer;
import com.example.engineer.View.Elements.DataManagers.UserSettingsManager;
import com.example.engineer.View.Elements.FXElementsProviders.FXDialogProvider;
import com.example.engineer.View.Elements.FXElementsProviders.FXIconLoader;
import com.example.engineer.View.Elements.FXElementsProviders.FXMLViewLoader;
import com.example.engineer.View.Elements.FXElementsProviders.FileChooserProvider;
import com.example.engineer.View.Elements.Language.Dictionary;
import com.example.engineer.View.Elements.Language.LanguageChangeListener;
import com.example.engineer.View.Elements.Language.LanguageEntry;
import com.example.engineer.View.Elements.Language.LanguageManager;
import com.example.engineer.View.Elements.UpdateTableEvent.UpdateTableEventDispatcher;
import com.example.engineer.View.Elements.UpdateTableEvent.UpdateTableListener;
import com.example.engineer.View.FXViews.TagDetails.TagDetailsController;
import com.example.engineer.View.FXViews.VideoDetails.VideoManagementDetailsController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope("prototype")
public class SettingsController implements UpdateTableListener, LanguageChangeListener {
    @FXML
    private TableView<TableEntry> codeTable;
    @FXML
    private TableColumn<TableEntry, String> codeColumn;
    @FXML
    private TableColumn<TableEntry, Double> valueColumn;
    @FXML
    private TableColumn<TableEntry, String> descriptionColumn;
    @FXML
    private TableColumn<TableEntry, Void> editColumn;
    @FXML
    private TableColumn<TableEntry, Void> deleteColumn;
    @FXML
    private BorderPane settingsView;
    @FXML
    private CheckBox showHiddenTagsCheckBox, openRecentCheckBox, languageExportCheckBox;
    @FXML
    private ComboBox<LanguageEntry> languageBox;
    @FXML
    private Button addCodeButton,addCodesButton,hideCodesButton,unhideCodesButton,deleteCodesButton,manageButton;

    @Autowired
    SettingsService viewService;
    @Autowired
    OpenViewsInformationContainer openViews;
    @Autowired
    UserSettingsManager userSettings;

    private final Map<KeyCombination,Runnable> keyActions = new HashMap<>();


    public void initialize() {
        UpdateTableEventDispatcher.register(this);
        LanguageManager.register(this);

        //set up multiselect
        codeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Set up columns
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        codeColumn.setText(Dictionary.get("name"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        descriptionColumn.setText(Dictionary.get("description"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        valueColumn.setText(Dictionary.get("value"));

        codeTable.setPlaceholder(new Label(Dictionary.get("placeholder.codes")));

        // Set up edit column
        editColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<TableEntry, Void> call(final TableColumn<TableEntry, Void> param) {
                return new TableCell<>() {
                    private final Button editButton = new Button();
                    private final HBox centeredBox = new HBox(editButton);

                    {
                        editButton.setOnAction(event -> {
                            TableEntry code = getTableView().getItems().get(getIndex());
                            onEdit(code.getId());
                        });
                        editButton.setGraphic(new ImageView(FXIconLoader.getSmallIcon("edit.png")));

                        // Center-align the button within the HBox
                        centeredBox.setAlignment(Pos.CENTER);
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(centeredBox);
                        }
                    }
                };
            }
        });

        // Set up delete button
        deleteColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<TableEntry, Void> call(final TableColumn<TableEntry, Void> param) {
                return new TableCell<>() {
                    private final Button editButton = new Button();
                    private final HBox centeredBox = new HBox(editButton);

                    {
                        editButton.setOnAction(event -> {
                            TableEntry code = getTableView().getItems().get(getIndex());
                            deleteTag(code);
                        });
                        editButton.setGraphic(new ImageView(FXIconLoader.getSmallIcon("bin.png")));

                        // Center-align the button within the HBox
                        centeredBox.setAlignment(Pos.CENTER);
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(centeredBox);
                        }
                    }
                };
            }
        });

        showHiddenTagsCheckBox.setSelected(userSettings.ShowHidden());
        showHiddenTagsCheckBox.setText(Dictionary.get("settings.user.hidden"));
        showHiddenTagsCheckBox.setOnMouseClicked(event -> handleShowHiddenTags());

        openRecentCheckBox.setSelected(userSettings.openRecent());
        openRecentCheckBox.setText(Dictionary.get("settings.user.recent"));
        openRecentCheckBox.setOnMouseClicked(event -> handleOpenRecent());

        languageExportCheckBox.setSelected(userSettings.useDefaultLanguage());
        languageExportCheckBox.setText(Dictionary.get("settings.user.export"));
        languageExportCheckBox.setOnMouseClicked(event -> handleChosenLanguage());

        // Add data to the TableView
        codeTable.setItems(viewService.getExistingTags());

        //language box
        languageBox.getItems().addAll(viewService.getLanguages());
        languageBox.setCellFactory(cb -> new LanguageCell());
        languageBox.setButtonCell(new LanguageCell());
        languageBox.getSelectionModel().select(viewService.getCurrentLanguage());
        languageBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                var languageCode = newValue.getCode();
                userSettings.setLanguage(languageCode);
            }
        });

        addCodeButton.setText(Dictionary.get("settings.button.add"));
        addCodesButton.setText(Dictionary.get("settings.button.add.multi"));
        hideCodesButton.setText(Dictionary.get("settings.button.hide"));
        unhideCodesButton.setText(Dictionary.get("settings.button.unhide"));
        deleteCodesButton.setText(Dictionary.get("settings.button.delete"));
        manageButton.setText(Dictionary.get("settings.button.list"));

        //kye binds
        keyActions.put(new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN), this::onShiftSPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.D, KeyCombination.SHIFT_DOWN), this::onShiftDPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.T, KeyCombination.SHIFT_DOWN), this::onShiftTPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.A, KeyCombination.SHIFT_DOWN), this::onShiftAPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.L, KeyCombination.SHIFT_DOWN), this::onShiftLPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN), this::onCtrlMPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN), this::onCtrlUPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN), this::onCtrlHPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN), this::onCtrlXPressed);

        //add key binds
        settingsView.addEventFilter(KeyEvent.KEY_PRESSED,this::handleKeyPressed);

        Platform.runLater(() -> {
            var stage = (Stage) settingsView.getScene().getWindow();
            stage.setOnCloseRequest(e -> {
                UpdateTableEventDispatcher.unregister(this);
                LanguageManager.unregister(this);
                openViews.closeSettings();
                viewService.close();
            });
        });
    }

    private void handleKeyPressed(KeyEvent event){
        keyActions.keySet().stream()
                .filter(k -> k.match(event))
                .findFirst()
                .ifPresent(k -> keyActions.get(k).run());
    }

    //CLOSE WINDOW
    private void onShiftSPressed() {
        var stage = (Stage) settingsView.getScene().getWindow();
        stage.close();
        viewService.close();
        openViews.closeSettings();
    }

    //OPEN TAG CREATION
    @FXML
    protected void onAdd(){
        openTagCreation();
    }

    private void onShiftAPressed() {
        openTagCreation();
    }

    private void openTagCreation(){
        if(!openViews.getCreateTag())
            try{
                var loader = FXMLViewLoader.getView("CreateTagViewModel");

                //load scene
                Parent root = loader.load();
                var createScene = new Scene(root);

                //new stage
                var secondaryStage = new Stage();
                secondaryStage.setScene(createScene);
                secondaryStage.setTitle("Create Tag");

                //make window modal
                secondaryStage.initOwner(settingsView.getScene().getWindow());
                secondaryStage.show();
                openViews.openCreateTag();
            }catch (Exception e){
                FXDialogProvider.errorDialog(e.getMessage());
            }
        else
            FXDialogProvider.errorDialog("Tag creation already open");
    }

    //ADD MULTIPLE TAGS FROM FILE
    @FXML
    protected void onMultiAdd(){
        loadTags();
    }

    private void onCtrlMPressed() {
        loadTags();
    }

    private void loadTags(){
        try{
            var path = FileChooserProvider.textFileChooser((Stage)settingsView.getScene().getWindow());
            viewService.loadBatchTags(path);
        }catch (Exception e){
            FXDialogProvider.errorDialog(e.getMessage());
        }
    }

    //HIDE TAGS
    @FXML
    protected void onHide(){
        hideTags();
    }

    private void onCtrlHPressed(){
        hideTags();
    }

    private void hideTags(){
        var selected = codeTable.getSelectionModel();

        if(selected.getSelectedItem() != null) {
            var ids = selected.getSelectedItems().stream()
                    .map(TableEntry::getId)
                    .toList();
            viewService.hideTags(ids);
        }else
            FXDialogProvider.errorDialog("No tags selected");
    }

    //UNHIDE TAGS
    @FXML
    protected void onUnhide(){
        unhideTags();
    }

    private void onCtrlUPressed(){
        unhideTags();
    }

    private void unhideTags(){
        var selected = codeTable.getSelectionModel();

        if(selected.getSelectedItem() != null) {
            var ids = selected.getSelectedItems().stream()
                    .map(TableEntry::getId)
                    .toList();
            viewService.unhideTags(ids);
        }else
            FXDialogProvider.errorDialog("No tags selected");
    }

    //DELETE TAGS
    private void deleteTag(TableEntry entry){
        codeTable.getItems().remove(entry);
        viewService.removeTag(entry.getId());
    }

    @FXML
    protected void onDelete(){
        deleteTags();
    }

    private void onCtrlXPressed(){
        deleteTags();
    }

    private void deleteTags(){
        var selected = codeTable.getSelectionModel();

        if(selected.getSelectedItem() != null) {
            var ids = selected.getSelectedItems().stream()
                    .map(TableEntry::getId)
                    .toList();
            viewService.removeTags(ids);
        }else
            FXDialogProvider.errorDialog("No tags selected");
    }

    //OPEN VIDEO MANAGEMENT LIST
    @FXML
    protected void onManage(){
        openVideoManagement();
    }

    private void onShiftLPressed(){
        openVideoManagement();
    }

    private void openVideoManagement(){
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
                secondaryStage.initOwner(settingsView.getScene().getWindow());
                secondaryStage.show();
                openViews.openVideoList();
            }catch(Exception e){
                FXDialogProvider.errorDialog(e.getMessage());
            }
    }

    //OPEN CURRENT VIDEO DETAILS
    private void onShiftDPressed() {
        try{
            var loader = FXMLViewLoader.getView("VideoManagementDetailsViewModel");

            //load scene
            Parent root = loader.load();
            var videoDetailsScene = new Scene(root);

            //get controller
            VideoManagementDetailsController videoController = loader.getController();
            var vid = viewService.getCurrentVideo();
            if(vid!=null)
                videoController.init(vid);
            else
                throw new Exception("No video is open");

            //new stage
            var secondaryStage = new Stage();
            secondaryStage.setScene(videoDetailsScene);
            secondaryStage.setTitle("Video Details");

            //make it a modal window
            secondaryStage.initOwner(settingsView.getScene().getWindow());
            secondaryStage.show();
        }catch (Exception e){
            FXDialogProvider.errorDialog(e.getMessage());
        }
    }

    //OPEN TAG EDIT
    private void onEdit(int id){
        openTagDetails(id);
    }

    private void onShiftTPressed() {
        var selected = codeTable.getSelectionModel();

        if (selected.getSelectedItem() != null)
            for(var s : selected.getSelectedItems())
                openTagDetails(s.getId());
        else
            FXDialogProvider.errorDialog("No tags selected");
    }

    private void openTagDetails(int id){
        try{
            var loader = FXMLViewLoader.getView("TagDetailsViewModel");

            //load scene
            Parent root = loader.load();
            var detailsScene = new Scene(root);

            //get controller
            TagDetailsController controller = loader.getController();
            controller.init(id);

            //new stage
            var secondaryStage = new Stage();
            secondaryStage.setScene(detailsScene);
            secondaryStage.setTitle("Tag Details");

            //make window modal
            secondaryStage.initOwner(settingsView.getScene().getWindow());
            secondaryStage.show();
            System.out.println("Opening Tag details");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //HANDLE SHOW HIDDEN TAGS
    private void handleShowHiddenTags(){
        viewService.changeShowHidden(showHiddenTagsCheckBox.isSelected());
    }

    //HANDLE OPEN RECENT
    private void handleOpenRecent(){
        userSettings.setOpenRecent(openRecentCheckBox.isSelected());
    }

    //HANDLE CHOSEN LANGUAGE
    private void handleChosenLanguage(){
        userSettings.setUseDefaultLanguage(languageExportCheckBox.isSelected());
    }

    @Override
    public void updateTable() {
        codeTable.setItems(viewService.getExistingTags());
    }

    @Override
    public void changeLanguage() {
        //change language box
        for(var e : languageBox.getItems())
            e.setLanguage();

        //table header
        codeColumn.setText(Dictionary.get("name"));
        valueColumn.setText(Dictionary.get("value"));
        descriptionColumn.setText(Dictionary.get("description"));

        //table placeholder
        codeTable.setPlaceholder(new Label(Dictionary.get("placeholder.codes")));

        //settings
        showHiddenTagsCheckBox.setText(Dictionary.get("settings.user.hidden"));
        openRecentCheckBox.setText(Dictionary.get("settings.user.recent"));
        languageExportCheckBox.setText(Dictionary.get("settings.user.export"));

        //buttons
        addCodeButton.setText(Dictionary.get("settings.button.add"));
        addCodesButton.setText(Dictionary.get("settings.button.add.multi"));
        hideCodesButton.setText(Dictionary.get("settings.button.hide"));
        unhideCodesButton.setText(Dictionary.get("settings.button.unhide"));
        deleteCodesButton.setText(Dictionary.get("settings.button.delete"));
        manageButton.setText(Dictionary.get("settings.button.list"));
    }
}

