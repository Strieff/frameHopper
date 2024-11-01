package com.example.engineer.View.FXViews.TagManager;

import com.example.engineer.View.Elements.Language.Dictionary;
import com.example.engineer.View.Elements.Language.LanguageChangeListener;
import com.example.engineer.View.Elements.OpenViewsInformationContainer;
import com.example.engineer.View.Elements.UpdateTableEvent.UpdateTableEventDispatcher;
import com.example.engineer.View.Elements.UpdateTableEvent.UpdateTableListener;
import jakarta.annotation.PostConstruct;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope("prototype")
public class TagManagerController implements LanguageChangeListener, UpdateTableListener {
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private Button nameSortButton;
    @FXML
    private Button valueSortButton;
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
    private BorderPane tagManagerView;

    private final ObservableList<TableEntry> items = FXCollections.observableArrayList();
    private final Map<KeyCombination,Runnable> keyActions = new HashMap<>();


    @Autowired
    TagManagerService viewService;
    @Autowired
    OpenViewsInformationContainer openViews;

    @PostConstruct
    public void init() {
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

        codeColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

        // Set up button actions
        nameSortButton.setOnAction(event -> handleNameSort());
        valueSortButton.setOnAction(event -> handleValueSort());
        cancelButton.setOnAction(event -> handleCancel());
        saveButton.setOnAction(event -> handleSave());
        searchButton.setOnAction(event -> handleSearch());

        //kye bind
        keyActions.put(new KeyCodeCombination(KeyCode.M, KeyCombination.SHIFT_DOWN), this::onShiftMPressed);

        //add key binds
        tagManagerView.addEventFilter(KeyEvent.KEY_PRESSED,this::handleKeyPressed);

        Platform.runLater(() -> {
            var stage = (Stage) tagManagerView.getScene().getWindow();
            stage.setOnCloseRequest(e -> {
                openViews.closeTagManager();
                viewService.close();
            });
            UpdateTableEventDispatcher.unregister(this);
            tagManagerView.requestFocus();
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

    private void onShiftMPressed() {
        var stage = (Stage) tagManagerView.getScene().getWindow();
        stage.close();
        openViews.closeTagManager();
        System.out.println("Shift + M pressed! Closing tag manager");
    }

    private void handleNameSort(){
        System.out.println("sort by name");
    }

    private void handleValueSort(){
        System.out.println("Sort by value");
    }

    private void handleCancel() {
        UpdateTableEventDispatcher.unregister(this);
        viewService.close();
        var stage = (Stage) tagManagerView.getScene().getWindow();
        stage.close();
        openViews.closeTagManager();
        System.out.println("Cancel button clicked");
    }

    private void handleSave() {
        viewService.save();
        viewService.close();
        var stage = (Stage) tagManagerView.getScene().getWindow();
        stage.close();
        openViews.closeTagManager();
        System.out.println("Save button clicked");
    }

    private void handleSearch() {
        String searchText = searchField.getText();
        System.out.println("Searching for: " + searchText);
        // Handle search functionality
    }

    @Override
    public void changeLanguage() {

    }

    @Override
    public void updateTable() {
        codeTable.setItems(viewService.getTags());
    }
}
