package com.FrameHopper.app.View.FXViews.Export;

import com.FrameHopper.app.Model.Video;
import com.FrameHopper.app.View.Elements.DataManagers.ViewContainer.ViewFlag;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXDialogProvider;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FileChooserProvider;
import com.FrameHopper.app.View.Elements.DataManagers.ViewContainer.OpenViewsInformationContainer;
import com.FrameHopper.app.settings.UserSettingsService;
import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.View.Elements.Language.LanguageChangeListener;
import com.FrameHopper.app.View.Elements.Language.LanguageManager;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
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
    @FXML
    private ListView<ExportActionEntry.VideoExportActionEntry> videoFieldsList;
    @FXML
    private ListView<ExportActionEntry.TagExportActionEntry> tagFieldsList;
    @FXML
    private TitledPane videoPane, videoDataPane, tagDataPane;
    @FXML
    private Accordion exportAccordion;

    private final ExportService viewService;
    private final OpenViewsInformationContainer viewContainer;
    private final UserSettingsService userSettingsService;

    private final Set<Integer> selectedIds = new HashSet<>();
    private final Map<KeyCombination,Runnable> keyActions = new HashMap<>();

    private Integer lastSelectedIndex;
    private boolean allSelected;
    private boolean isSearching;

    public ExportController(
            ExportService viewService,
            OpenViewsInformationContainer viewContainer,
            UserSettingsService userSettingsService
    ) {
        this.viewService = viewService;
        this.viewContainer = viewContainer;
        this.userSettingsService = userSettingsService;
        lastSelectedIndex = null;
        allSelected = false;
        isSearching = false;

        LanguageManager.register(this);
    }

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
                        selectedIds.add(entry.getId());
                    else
                        selectedIds.remove(entry.getId());

                    if (event.isShiftDown() && lastSelectedIndex != null) {
                        int start = Math.min(lastSelectedIndex, currentIndex);
                        int end = Math.max(lastSelectedIndex, currentIndex);
                        for (int i = start; i <= end; i++) {
                            var rangeEntry = getTableView().getItems().get(i);
                            rangeEntry.setSelected(true);
                            selectedIds.add(rangeEntry.getId());
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

        videoPane.setText(Dictionary.get("export.name"));

        //VIDEO DATA ACTIONS
        videoDataPane.setText(Dictionary.get("export.data.video"));
        videoFieldsList.getItems().setAll(
                new ExportActionEntry.VideoExportActionEntry("data.overview.frameCount", Video::getTotalFrames,
                        "data.overview.summary.frameCount"),
                new ExportActionEntry.VideoExportActionEntry("data.overview.codes", viewService::getTotalTagsOnVideo,
                        ""),
                new ExportActionEntry.VideoExportActionEntry("data.overview.runtime", Video::getDuration,
                        "data.overview.summary.runtime"),
                new ExportActionEntry.VideoExportActionEntry("data.overview.framerate", Video::getFrameRate,
                        ""),
                new ExportActionEntry.VideoExportActionEntry("data.overview.totalPoints", viewService::getTotalPoints,
                        "data.overview.summary.totalPoints"),
                new ExportActionEntry.VideoExportActionEntry("data.overview.complexity", viewService::getComplexity,
                        "data.overview.summary.complexity")
        );

        setupList(videoFieldsList);

        //TAG DATA ACTIONS
        tagDataPane.setText(Dictionary.get("export.data.tag"));
        tagFieldsList.getItems().setAll(
                new ExportActionEntry.TagExportActionEntry("data.tags.value", pair -> pair.getKey().getValue()),
                new ExportActionEntry.TagExportActionEntry("data.tags.amount", pair -> viewService.getAmount(pair.getKey(), pair.getValue())),
                new ExportActionEntry.TagExportActionEntry("data.tags.totalPoints", pair -> viewService.getTotalPoints(pair.getKey(), pair.getValue()))
        );

        setupList(tagFieldsList);

        exportAccordion.setExpandedPane(videoPane);

        //add key binds
        keyActions.put(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN), this::onCtrlAPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.E, KeyCombination.SHIFT_DOWN), this::handleClose);

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

    //ENABLE REORDERING FOR EXPORT ACTIONS
    private <T> void setupList(ListView<T> listView) {

        // Simple style for drop indicator
        listView.getStylesheets().add(
                getClass().getClassLoader().getResource("styling/export-table.css").toExternalForm()
        );


        final PseudoClass DROP_ABOVE = PseudoClass.getPseudoClass("drop-above");
        final PseudoClass DROP_BELOW = PseudoClass.getPseudoClass("drop-below");

        listView.setCellFactory(lv -> new ListCell<>() {

            private final Label handle = new Label("≡"); // drag handle
            private final CheckBox checkBox = new CheckBox();
            private final HBox root = new HBox(8, handle, checkBox);

            private ExportActionEntry boundItem;

            {
                // Make it obvious it's draggable
                handle.setStyle("-fx-cursor: hand; -fx-opacity: 0.75; -fx-font-size: 14;");
                root.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                // Start drag ONLY from handle (no need to “select” first)
                handle.setOnDragDetected(e -> {
                    if (isEmpty()) return;

                    // optionally select the row when dragging starts
                    listView.getSelectionModel().select(getIndex());

                    Dragboard db = startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent cc = new ClipboardContent();
                    cc.putString(Integer.toString(getIndex()));
                    db.setContent(cc);

                    e.consume();
                });

                // Clear indicator when drag exits
                setOnDragExited(e -> {
                    pseudoClassStateChanged(DROP_ABOVE, false);
                    pseudoClassStateChanged(DROP_BELOW, false);
                });

                setOnDragOver(e -> {
                    Dragboard db = e.getDragboard();
                    if (!db.hasString()) return;

                    int from = Integer.parseInt(db.getString());
                    if (from == getIndex()) return;

                    e.acceptTransferModes(TransferMode.MOVE);

                    // Decide whether user is hovering top or bottom half of this cell
                    boolean above = e.getY() < (getHeight() / 2.0);

                    pseudoClassStateChanged(DROP_ABOVE, above);
                    pseudoClassStateChanged(DROP_BELOW, !above);

                    e.consume();
                });

                setOnDragDropped(e -> {
                    // clear indicator
                    pseudoClassStateChanged(DROP_ABOVE, false);
                    pseudoClassStateChanged(DROP_BELOW, false);

                    Dragboard db = e.getDragboard();
                    if (!db.hasString()) return;

                    int from = Integer.parseInt(db.getString());
                    if (from < 0) return;

                    boolean above = e.getY() < (getHeight() / 2.0);

                    ObservableList<T> items = listView.getItems();
                    T moved = items.remove(from);

                    int to;
                    if (isEmpty()) {
                        to = items.size();
                    } else {
                        to = getIndex();
                        if (!above) to++; // dropping below means insert after this index
                    }

                    // adjust after removal
                    if (to > from) to--;

                    // clamp
                    to = Math.max(0, Math.min(to, items.size()));

                    items.add(to, moved);
                    listView.getSelectionModel().select(to);

                    e.setDropCompleted(true);
                    e.consume();
                });

                // If you want: allow dropping into empty space at bottom
                listView.setOnDragOver(e -> {
                    Dragboard db = e.getDragboard();
                    if (db.hasString()) e.acceptTransferModes(TransferMode.MOVE);
                    e.consume();
                });

                listView.setOnDragDropped(e -> {
                    Dragboard db = e.getDragboard();
                    if (!db.hasString()) return;

                    int from = Integer.parseInt(db.getString());
                    ObservableList<T> items = listView.getItems();
                    T moved = items.remove(from);
                    items.add(moved); // drop at end

                    listView.getSelectionModel().select(items.size() - 1);
                    e.setDropCompleted(true);
                    e.consume();
                });
            }

            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                // IMPORTANT: always clear drop indicator on reuse
                pseudoClassStateChanged(DROP_ABOVE, false);
                pseudoClassStateChanged(DROP_BELOW, false);

                if (boundItem != null) {
                    checkBox.selectedProperty().unbindBidirectional(boundItem.getSelected());
                    boundItem = null;
                }

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    boundItem = (ExportActionEntry) item;
                    checkBox.setText(Dictionary.get(((ExportActionEntry) item).getLabelName()));
                    checkBox.selectedProperty().bindBidirectional(((ExportActionEntry) item).getSelected());
                    setGraphic(root);
                }
            }
        });
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
        viewContainer.close(ViewFlag.EXPORT);
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

            var path = FileChooserProvider.locationFileChooser((Stage)videoTable.getScene().getWindow(), userSettingsService.getExportPath());

            var languageCode = userSettingsService.useDefaultLanguage() ? userSettingsService.getLanguage() : FXDialogProvider.languageDialog();

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

            var filePath = path + File.separator + name;
            viewService.exportData(
                    filePath,
                    selectedIds.stream().toList(),
                    new LinkedList<>(videoFieldsList.getItems()),
                    new LinkedList<>(tagFieldsList.getItems()),
                    format,
                    languageCode
            );

            userSettingsService.setExportRecent(path);
            FXDialogProvider.messageDialog(Dictionary.get("message.export.complete"));
        }catch (RuntimeException e){
            e.printStackTrace();
            FXDialogProvider.messageDialog(Dictionary.get("cancelled"));
        }catch (Exception e) {
            e.printStackTrace();
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
        videoPane.setText(Dictionary.get("export.name"));
        videoDataPane.setText(Dictionary.get("export.data.video"));
        tagDataPane.setText(Dictionary.get("export.data.tag"));

        //update export options
        tagFieldsList.refresh();
        videoFieldsList.refresh();
    }
}