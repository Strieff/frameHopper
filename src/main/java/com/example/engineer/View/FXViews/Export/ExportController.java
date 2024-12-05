package com.example.engineer.View.FXViews.Export;

import com.example.engineer.View.Elements.FXElementsProviders.FXDialogProvider;
import com.example.engineer.View.Elements.FXElementsProviders.FileChooserProvider;
import com.example.engineer.View.Elements.DataManagers.OpenViewsInformationContainer;
import com.example.engineer.View.Elements.DataManagers.UserSettingsManager;
import com.example.engineer.View.Elements.Language.Dictionary;
import com.example.engineer.View.Elements.Language.LanguageChangeListener;
import com.example.engineer.View.Elements.Language.LanguageManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Component
@Scope("prototype")
public class ExportController implements LanguageChangeListener {
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
    @Autowired
    private UserSettingsManager userSettings;

    @FXML
    public void initialize() {
        LanguageManager.register(this);

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
        videoNameColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<TableEntry, String> call(TableColumn<TableEntry, String> param) {
                return new TableCell<>() {
                    private final Text text = new Text();

                    {
                        text.wrappingWidthProperty().bind(videoNameColumn.widthProperty());
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
        videoNameColumn.setText(Dictionary.get("export.name"));

        //clear button
        clearButton.setOnAction(event -> deselectAll());
        clearButton.setText(Dictionary.get("export.clear"));

        //cancel button
        cancelButton.setOnAction(event -> handleClose());
        cancelButton.setText(Dictionary.get("cancel"));

        //search button
        searchButton.setOnAction(event -> handleSearch());
        searchField.setPromptText(Dictionary.get("search"));

        //export button
        exportButton.setOnAction(event -> handleExport());
        exportButton.setText(Dictionary.get("export.export"));

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
        LanguageManager.unregister(this);
        var stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
        openViews.closeExport();
    }

    //HANDLE SEARCH
    private void handleSearch() {
        if(!searchField.getText().isEmpty()) {
            if (!isSearching) {
                videoTable.setItems(viewService.getFiltered(videoTable.getItems(), searchField.getText()));
                searchButton.setText("X");
            } else {
                videoTable.setItems(viewService.getVideos(selectedIds));
                searchButton.setText("\uD83D\uDD0D");
                searchField.clear();
            }

            isSearching = !isSearching;
        }
    }

    //HANDLE EXPORT
    private void handleExport(){
        try {
            if(selectedIds.isEmpty()) throw new Exception(Dictionary.get("error.export.no-video"));

            var path = FileChooserProvider.locationFileChooser((Stage)videoTable.getScene().getWindow(), userSettings.getExportPath());

            var format = FXDialogProvider.customDialog(Dictionary.get("dialog.export.format"),-1,"EXCEL","CSV");
            if (format == -1) throw new RuntimeException();

            var name = FXDialogProvider.inputDialog();
            if(name.isBlank()) throw new Exception(Dictionary.get("error.export.no-name"));

            while(
                    (format == 1 && new File(path+File.separator+name).exists()) ||
                    (format == 0 && new File(path+File.separator+name+".xlsx").exists())
            ) {
                var res = FXDialogProvider.customDialog(
                        Dictionary.get("dialog.export.exists"),
                        0,
                        Dictionary.get("cancel"),
                        Dictionary.get("dialog.export.option.rename"),
                        Dictionary.get("dialog.export.option.overwrite")
                );

                switch (res) {
                    case 0:
                        throw new RuntimeException();
                    case 1:
                        name = FXDialogProvider.inputDialog();
                        if(name.isBlank()) throw new Exception(Dictionary.get("error.export.no-name"));
                        break;
                }

                if(res == 2) break;
            }

            viewService.exportData(path+File.separator+name,selectedIds,format);
            userSettings.setExportRecent(path);
            FXDialogProvider.messageDialog(Dictionary.get("message.export.complete"));
        }catch (RuntimeException e){
            FXDialogProvider.messageDialog(Dictionary.get("cancelled"));
        }catch (Exception e) {
            FXDialogProvider.errorDialog(e.getMessage());
        }


    }

    @Override
    public void changeLanguage() {
        videoNameColumn.setText(Dictionary.get("export.name"));
        searchField.setPromptText(Dictionary.get("search"));
        cancelButton.setText(Dictionary.get("cancel"));
        clearButton.setText(Dictionary.get("export.clear"));
        exportButton.setText(Dictionary.get("export.export"));
    }
}