package com.FrameHopper.app.View.FXViews.TagManager;

import com.FrameHopper.app.View.Elements.DataManagers.ViewContainer.OpenViewsInformationContainer;
import com.FrameHopper.app.settings.UserSettingsService;
import com.FrameHopper.app.View.Elements.DataManagers.ViewContainer.ViewFlag;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXDialogProvider;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXIconLoader;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXMLViewLoader;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FileChooserProvider;
import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.View.Elements.Language.LanguageChangeListener;
import com.FrameHopper.app.View.Elements.Language.LanguageManager;
import com.FrameHopper.app.View.Elements.UpdateTableEvent.UpdateTableEventDispatcher;
import com.FrameHopper.app.View.Elements.UpdateTableEvent.UpdateTableListener;
import com.FrameHopper.app.View.FXViews.TagDetails.TagDetailsController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope("prototype")
public class TagManagerController implements LanguageChangeListener, UpdateTableListener {
    @FXML
    private TableView<TableEntry> codeTable;
    @FXML
    private TableColumn<TableEntry, String> codeColumn;
    @FXML
    private TableColumn<TableEntry, Double> valueColumn;
    @FXML
    private TableColumn<TableEntry, String> descriptionColumn;
    @FXML
    private TableColumn<TableEntry, Void> editColumn, deleteColumn;
    @FXML
    private BorderPane tagManagerView;
    @FXML
    private Button
            addCodeButton,
            addCodesButton,
            hideCodesButton,
            unhideCodesButton,
            deleteCodesButton;

    private final TagManagerService viewService;
    private final OpenViewsInformationContainer viewContainer;
    private final UserSettingsService userSettingsService;

    private final Map<KeyCombination,Runnable> keyActions = new HashMap<>();

    public TagManagerController(
            TagManagerService viewService,
            OpenViewsInformationContainer viewContainer,
            UserSettingsService userSettingsService
    ) {
        this.viewService = viewService;
        this.viewContainer = viewContainer;
        this.userSettingsService = userSettingsService;

        UpdateTableEventDispatcher.register(this);
        LanguageManager.register(this);
    }

    public void initialize() {
        //set up multiselect
        codeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Set up columns
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        codeColumn.setText(Dictionary.get("name"));
        codeColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<TableEntry, String> call(TableColumn<TableEntry, String> param) {
                return new TableCell<>() {
                    private final Text text = new Text();

                    {
                        text.wrappingWidthProperty().bind(codeColumn.widthProperty());
                        setGraphic(text);
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            text.setText(null);
                        }else{
                            text.setText(item);
                        }
                    }
                };
            }
        });
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        descriptionColumn.setText(Dictionary.get("description"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<TableEntry, String> call(TableColumn<TableEntry, String> param) {
                return new TableCell<>() {
                    private final Text text = new Text();

                    {
                        text.wrappingWidthProperty().bind(descriptionColumn.widthProperty());
                        setGraphic(text);
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            text.setText(null);
                        }else{
                            text.setText(item);
                        }
                    }
                };
            }
        });
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

        // Add data to the TableView
        codeTable.setItems(viewService.getExistingTags());

        addCodeButton.setText(Dictionary.get("settings.button.add"));
        addCodesButton.setText(Dictionary.get("settings.button.add.multi"));
        hideCodesButton.setText(Dictionary.get("settings.button.hide"));
        unhideCodesButton.setText(Dictionary.get("settings.button.unhide"));
        deleteCodesButton.setText(Dictionary.get("settings.button.delete"));

        //kye binds
        keyActions.put(new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN), this::onShiftSPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.T, KeyCombination.SHIFT_DOWN), this::onShiftTPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.A, KeyCombination.SHIFT_DOWN), this::onShiftAPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN), this::onCtrlMPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN), this::onCtrlUPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN), this::onCtrlHPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN), this::onCtrlXPressed);

        //add key binds
        tagManagerView.addEventFilter(KeyEvent.KEY_PRESSED,this::handleKeyPressed);

        Platform.runLater(() -> {
            var stage = (Stage) tagManagerView.getScene().getWindow();
            stage.setOnCloseRequest(e -> {
                UpdateTableEventDispatcher.unregister(this);
                LanguageManager.unregister(this);
                viewContainer.close(ViewFlag.TAG_MANAGER);
            });
        });
    }

    private void handleKeyPressed(KeyEvent event){
        keyActions.keySet().stream()
                .filter(k -> k.match(event))
                .findFirst()
                .ifPresent(k -> keyActions.get(k).run());
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
        if(viewContainer.isClosed(ViewFlag.CREATE_TAG)) {
            FXMLViewLoader.getView(
                    "CreateTagViewModel",
                    "Create Tag",
                    tagManagerView
            );
            viewContainer.open(ViewFlag.CREATE_TAG);
        }
        else
            FXDialogProvider.errorDialog(Dictionary.get("open.tag-creation"));
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
            var path = FileChooserProvider.textFileChooser((Stage)tagManagerView.getScene().getWindow());
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

        if(!userSettingsService.showSettingsWarning() || FXDialogProvider.yesNoDialog(String.format(Dictionary.get("warning.settings.hide"),getSelectedList()))) {
            if (selected.getSelectedItem() != null) {
                var ids = selected.getSelectedItems().stream()
                        .map(TableEntry::getId)
                        .toList();
                viewService.hideTags(ids);
            } else
                FXDialogProvider.errorDialog(Dictionary.get("error.settings.no-tags"));
        }
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

        if(!userSettingsService.showSettingsWarning() || FXDialogProvider.yesNoDialog(String.format(Dictionary.get("warning.settings.unhide"),getSelectedList()))) {
            if (selected.getSelectedItem() != null) {
                var ids = selected.getSelectedItems().stream()
                        .map(TableEntry::getId)
                        .toList();
                viewService.unhideTags(ids);
            } else
                FXDialogProvider.errorDialog(Dictionary.get("error.settings.no-tags"));
        }
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

        if(!userSettingsService.showSettingsWarning() || FXDialogProvider.yesNoDialog(String.format(Dictionary.get("warning.settings.delete"),getSelectedList()))) {
            if (selected.getSelectedItem() != null) {
                var ids = selected.getSelectedItems().stream()
                        .map(TableEntry::getId)
                        .toList();
                viewService.removeTags(ids);
            } else
                FXDialogProvider.errorDialog(Dictionary.get("error.settings.no-tags"));
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
            FXDialogProvider.errorDialog(Dictionary.get("error.settings.no-tags"));
    }

    private void openTagDetails(int id){
        var loader = FXMLViewLoader.getView(
                "TagDetailsViewModel",
                "Tag Details",
                tagManagerView
        );

        //get controller
        TagDetailsController controller = loader.getController();
        controller.init(id);
    }

    //get selected as string
    private String getSelectedList() {
        var list = new StringBuilder();
        for (var s : codeTable.getSelectionModel().getSelectedItems())
            list.append("\n").append(s.getCode());
        return list.toString();
    }

    //CLOSE WINDOW
    private void onShiftSPressed() {
        LanguageManager.unregister(this);
        var stage = (Stage) tagManagerView.getScene().getWindow();
        stage.close();
        viewContainer.close(ViewFlag.TAG_MANAGER);
    }



    @Override
    public void updateTable() {
        codeTable.setItems(viewService.getExistingTags());
    }

    @Override
    public void changeLanguage() {
        //table header
        codeColumn.setText(Dictionary.get("name"));
        valueColumn.setText(Dictionary.get("value"));
        descriptionColumn.setText(Dictionary.get("description"));

        //table placeholder
        codeTable.setPlaceholder(new Label(Dictionary.get("placeholder.codes")));

        //buttons
        addCodeButton.setText(Dictionary.get("settings.button.add"));
        addCodesButton.setText(Dictionary.get("settings.button.add.multi"));
        hideCodesButton.setText(Dictionary.get("settings.button.hide"));
        unhideCodesButton.setText(Dictionary.get("settings.button.unhide"));
        deleteCodesButton.setText(Dictionary.get("settings.button.delete"));
    }
}
