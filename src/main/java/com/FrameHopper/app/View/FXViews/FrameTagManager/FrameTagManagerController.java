package com.FrameHopper.app.View.FXViews.FrameTagManager;

import com.FrameHopper.app.View.Elements.DataManagers.ViewContainer.ViewFlag;
import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.View.Elements.Language.LanguageChangeListener;
import com.FrameHopper.app.View.Elements.DataManagers.ViewContainer.OpenViewsInformationContainer;
import com.FrameHopper.app.View.Elements.Language.LanguageManager;
import com.FrameHopper.app.View.Elements.UpdateTableEvent.UpdateTableEventDispatcher;
import com.FrameHopper.app.View.Elements.UpdateTableEvent.UpdateTableListener;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope("prototype")
public class FrameTagManagerController implements LanguageChangeListener, UpdateTableListener {
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private TableView<TableEntry> codeTable;
    @FXML
    private TableColumn<TableEntry, Boolean> selectColumn;
    @FXML
    private TableColumn<TableEntry, String> codeColumn;
    @FXML
    private TableColumn<TableEntry, Double> valueColumn;
    @FXML
    private Button cancelButton;
    @FXML
    private Button saveButton;
    @FXML
    private Label frameLabel;
    @FXML
    private BorderPane frameTagManagerView;

    private final FrameTagManagerService viewService;
    private final OpenViewsInformationContainer viewContainer;

    private final ObservableList<TableEntry> items = FXCollections.observableArrayList();
    private final Map<KeyCombination,Runnable> keyActions = new HashMap<>();

    private boolean isSearching;

    public FrameTagManagerController(FrameTagManagerService viewService, OpenViewsInformationContainer viewContainer) {
        this.viewService = viewService;
        this.viewContainer = viewContainer;
        isSearching = false;

        LanguageManager.register(this);
        UpdateTableEventDispatcher.register(this);
    }

    @FXML
    public void initialize() {
        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn((Integer index) -> {
            TableEntry entry = codeTable.getItems().get(index);

            // Add listener only if it hasn't been added before
            if (!entry.isHasListener()) {
                entry.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        viewService.add(entry.getId());
                        System.out.println("selected, id: " + entry.getId());
                    } else {
                        viewService.remove(entry.getId());
                        System.out.println("deselected, id: " + entry.getId());
                    }
                });
                entry.setHasListener(true); // mark listener as added
            }

            return entry.selectedProperty();
        }));

        searchField.setPromptText(Dictionary.get("search"));
        codeTable.setPlaceholder(new Label(Dictionary.get("placeholder.codes")));

        codeColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        codeColumn.setText(Dictionary.get("name"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueColumn.setText(Dictionary.get("value"));

        // Set up button actions
        cancelButton.setOnAction(event -> handleCancel());
        saveButton.setText(Dictionary.get("save"));
        saveButton.setOnAction(event -> handleSave());
        cancelButton.setText(Dictionary.get("cancel"));
        searchButton.setOnAction(event -> handleSearch());

        //kye bind
        keyActions.put(new KeyCodeCombination(KeyCode.M, KeyCombination.SHIFT_DOWN), this::onShiftMPressed);

        //add key binds
        frameTagManagerView.addEventFilter(KeyEvent.KEY_PRESSED,this::handleKeyPressed);

        Platform.runLater(() -> {
            var stage = (Stage) frameTagManagerView.getScene().getWindow();
            stage.setOnCloseRequest(e -> {
                LanguageManager.unregister(this);
                UpdateTableEventDispatcher.unregister(this);
                viewContainer.close(ViewFlag.FRAME_TAG_MANAGER);
                viewService.close();
            });
            frameTagManagerView.requestFocus();
        });
    }

    public void init(int frameNo){
        frameLabel.setText(String.format(
                Dictionary.get("tm.frame"),
                (frameNo + 1)
        ));

        // Populate the table with data
        items.addAll(viewService.getTags());
        codeTable.setItems(items);
    }

    private void handleKeyPressed(KeyEvent event){
        keyActions.keySet().stream()
                .filter(k -> k.match(event))
                .findFirst()
                .ifPresent(k -> keyActions.get(k).run());
    }

    //CLOSE TAG MANAGER
    private void onShiftMPressed() {
        var stage = (Stage) frameTagManagerView.getScene().getWindow();
        stage.close();
        viewContainer.close(ViewFlag.FRAME_TAG_MANAGER);
        System.out.println("Shift + M pressed! Closing tag manager");
    }

    //HANDLE CANCEL
    private void handleCancel() {
        UpdateTableEventDispatcher.unregister(this);
        viewService.close();
        LanguageManager.unregister(this);
        var stage = (Stage) frameTagManagerView.getScene().getWindow();
        stage.close();
        viewContainer.close(ViewFlag.FRAME_TAG_MANAGER);
        System.out.println("Cancel button clicked");
    }

    //HANDLE SAVE
    private void handleSave() {
        viewService.save();
        viewService.close();
        LanguageManager.unregister(this);
        var stage = (Stage) frameTagManagerView.getScene().getWindow();
        stage.close();
        viewContainer.close(ViewFlag.FRAME_TAG_MANAGER);
        System.out.println("Save button clicked");
    }

    //HANDLE SEARCH
    private void handleSearch() {
        if(!searchField.getText().isEmpty()) {
            if (!isSearching) {
                codeTable.setItems(viewService.getFiltered(codeTable.getItems(),searchField.getText()));
                searchButton.setText("X");
            } else {
                codeTable.setItems(items);
                searchButton.setText("\uD83D\uDD0D");
                searchField.clear();
            }

            isSearching = !isSearching;
        }
    }

    @Override
    public void changeLanguage() {
        var frameNo=Integer.parseInt(frameLabel.getText().split(" ")[1]);
        frameLabel.setText(String.format(Dictionary.get("tm.frame"), frameNo));
        saveButton.setText(Dictionary.get("save"));
        cancelButton.setText(Dictionary.get("cancel"));
        codeColumn.setText(Dictionary.get("name"));
        valueColumn.setText(Dictionary.get("value"));
        searchField.setPromptText(Dictionary.get("search"));
        codeTable.setPlaceholder(new Label(Dictionary.get("placeholder.codes")));
        codeTable.setItems(viewService.getTags());
    }

    @Override
    public void updateTable() {
        codeTable.setItems(viewService.getTags());
    }
}
