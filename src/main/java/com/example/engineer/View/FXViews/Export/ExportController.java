package com.example.engineer.View.FXViews.Export;

import com.example.engineer.View.Elements.OpenViewsInformationContainer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

import java.util.*;

@Component
@Scope("prototype")
public class ExportController {
    @FXML
    private TableView<TableEntry> videoTable;
    @FXML
    private TableColumn<TableEntry, Boolean> selectColumn;
    @FXML
    private TableColumn<TableEntry, String> videoNameColumn;
    @FXML
    private TextField searchField;
    @FXML
    private Button clearButton, cancelButton, exportButton, searchButton;
    @FXML
    private BorderPane exportView;

    @Autowired
    ExportService viewService;
    @Autowired
    OpenViewsInformationContainer openViews;

    private Integer lastSelectedIndex = null;
    private boolean allSelected = false;
    private boolean isSearching = false;
    private final Set<Integer> selectedIds = new HashSet<>();
    private final Map<KeyCombination,Runnable> keyActions = new HashMap<>();

    @FXML
    public void initialize() {
        videoTable.setItems(viewService.getVideos());

        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectColumn.setCellFactory(tc -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnMouseClicked(event -> {
                    var currentIndex = getIndex();
                    var entry = getTableView().getItems().get(currentIndex);
                    entry.setSelected(checkBox.isSelected());

                    if(checkBox.isSelected())
                        selectedIds.add(Integer.valueOf(entry.getId()));
                    else
                        selectedIds.remove(Integer.valueOf(entry.getId()));

                    if (event.isShiftDown() && lastSelectedIndex != null) {
                        int start = Math.min(lastSelectedIndex, currentIndex);
                        int end = Math.max(lastSelectedIndex, currentIndex);
                        for (int i = start; i <= end; i++) {
                            var rangeEntry = getTableView().getItems().get(i);
                            rangeEntry.setSelected(true);
                            selectedIds.add(Integer.valueOf(rangeEntry.getId()));
                        }
                    }
                    lastSelectedIndex = currentIndex;
                });
            }

            @Override
            protected void updateItem(Boolean item,boolean empty) {
                super.updateItem(item, empty);
                if(empty)
                    setGraphic(null);
                else{
                    checkBox.setSelected(item != null && item);
                    setGraphic(checkBox);
                }
            }
        });

        videoNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        //clear button
        clearButton.setOnAction(event -> deselectAll());

        //cancel button
        cancelButton.setOnAction(event -> handleClose());

        //search button
        searchButton.setOnAction(event -> handleSearch());

        //export button
        exportButton.setOnAction(event -> handleExport());

        keyActions.put(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN), this::onCtrlAPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.E, KeyCombination.SHIFT_DOWN), this::onCtrlEPressed);

        //add key binds
        exportView.addEventFilter(KeyEvent.KEY_PRESSED,this::handleKeyPressed);

        Platform.runLater(() -> {
            var stage = (Stage) cancelButton.getScene().getWindow();
            stage.setOnCloseRequest(e -> handleClose());
        });
    }

    //HANDLE KEY BINDS
    private void handleKeyPressed(KeyEvent event){
        keyActions.keySet().stream()
                .filter(k -> k.match(event))
                .findFirst()
                .ifPresent(k -> keyActions.get(k).run());
    }

    //CLOSE SETTINGS
    private void onCtrlEPressed() {
        handleClose();
    }

    //SELECT/DESELECT ALL
    private void onCtrlAPressed(){
        if (allSelected)
            deselectAll();
        else
            for (var e : videoTable.getItems()) {
                selectedIds.add(e.getId());
                e.setSelected(true);
            }
        allSelected = !allSelected;
    }

    private void deselectAll(){
        videoTable.getItems().forEach(e -> e.setSelected(false));
        selectedIds.clear();
    }

    //HANDLE CLOSE
    private void handleClose(){
        var stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
        openViews.closeExport();
    }

    //HANDLE SEARCH
    private void handleSearch() {
        if(!searchField.getText().isEmpty())
            if(!isSearching) {
                    videoTable.setItems(viewService.getFiltered(videoTable.getItems(), searchField.getText()));
                    searchButton.setText("X");
                    isSearching = true;
            }else{
                videoTable.setItems(viewService.getVideos(selectedIds));
                searchButton.setText("\uD83D\uDD0D");
                searchField.clear();
                isSearching = false;
            }
    }

    //HANDLE EXPORT
    private void handleExport(){

    }
}