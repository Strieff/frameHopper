package com.FrameHopper.app.ui.controller;

import com.FrameHopper.app.adapters.DataExportAdapter;
import com.FrameHopper.app.boundry.dto.export.ChosenTagAnalytics;
import com.FrameHopper.app.boundry.dto.export.ChosenVideoAnalytics;
import com.FrameHopper.app.boundry.dto.export.ExportDataInput;
import com.FrameHopper.app.core.ports.out.export.CSVExportPort;
import com.FrameHopper.app.core.ports.out.export.ExcelExportPort;
import com.FrameHopper.app.ui.settings.UserSettingsAdapter;
import com.FrameHopper.app.boundry.dto.FrameDTO;
import com.FrameHopper.app.boundry.dto.VideoDTO;
import com.FrameHopper.app.core.ports.in.frame.FrameQuery;
import com.FrameHopper.app.core.ports.in.video.VideoQuery;
import com.FrameHopper.app.ui.UIFlag;
import com.FrameHopper.app.ui.UIManager;
import com.FrameHopper.app.ui.dialog.FileChooserProvider;
import com.FrameHopper.app.ui.eventing.VideoDeletedEventDispatcher;
import com.FrameHopper.app.ui.eventing.VideoDeletedEventListener;
import com.FrameHopper.app.ui.eventing.VideoPathUpdatedEventDispatcher;
import com.FrameHopper.app.ui.eventing.VideoPathUpdatedListener;
import com.FrameHopper.app.ui.utils.SearchUtils;
import com.FrameHopper.app.ui.ve.ExportActionEntry;
import com.FrameHopper.app.ui.UiView;
import com.FrameHopper.app.ui.ve.ExportTableEntry;
import com.FrameHopper.app.ui.ve.TagExportActionEntry;
import com.FrameHopper.app.ui.ve.VideoExportActionEntry;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class ExportController extends UiView implements
        VideoDeletedEventListener,
        VideoPathUpdatedListener
{
    @FXML
    private TableView<ExportTableEntry> videoTable;
    @FXML
    private TableColumn<ExportTableEntry, Boolean> selectColumn;
    @FXML
    private TableColumn<ExportTableEntry, String> videoNameColumn;
    @FXML
    private TextField searchField;
    @FXML
    private Button clearButton, cancelButton, exportButton, searchButton;
    @FXML
    private BorderPane exportView;
    @FXML
    private ListView<VideoExportActionEntry> videoFieldsList;
    @FXML
    private ListView<TagExportActionEntry> tagFieldsList;
    @FXML
    private TitledPane videoPane, videoDataPane, tagDataPane;
    @FXML
    private Accordion exportAccordion;
    @FXML
    private ComboBox<String> fileTypeBox;

    private final VideoQuery videoQuery;
    private final FrameQuery frameQuery;
    private final UserSettingsAdapter userSettingsAdapter;
    private final CSVExportPort csvExportPort;
    private final ExcelExportPort excelExportPort;
    private final UIManager uiManager;

    private Integer lastSelectedIndex = null;

    private ObservableList<ExportTableEntry> cachedVideoList;

    public ExportController(
            FrameQuery frameQuery,
            UserSettingsAdapter userSettingsAdapter,
            UIManager uiManager,
            VideoQuery videoQuery,
            DataExportAdapter dataExportAdapter
    ) {
        VideoDeletedEventDispatcher.register(this);
        VideoPathUpdatedEventDispatcher.register(this);

        this.frameQuery = frameQuery;
        this.userSettingsAdapter = userSettingsAdapter;
        this.uiManager = uiManager;
        this.videoQuery = videoQuery;
        this.csvExportPort = dataExportAdapter;
        this.excelExportPort = dataExportAdapter;
    }

    @FXML
    public void initialize() {
        exportView.setOnMouseClicked(e -> exportView.requestFocus());

        videoNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        bind(videoNameColumn, "export.video.table.name");
        videoNameColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<ExportTableEntry, String> call(TableColumn<ExportTableEntry, String> param) {
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

        selectColumn.setCellValueFactory(cd -> cd.getValue().selectedProperty());
        selectColumn.setCellFactory(tc -> new TableCell<>() {

            private final CheckBox checkBox = new CheckBox();
            private BooleanProperty boundTo;

            {
                checkBox.setOnMouseClicked(event -> {
                    int currentIndex = getIndex();

                    // IMPORTANT: update the clicked row too if you override behavior
                    var rowItem = getTableView().getItems().get(currentIndex);
                    rowItem.setSelected(checkBox.isSelected());

                    if (event.isShiftDown() && lastSelectedIndex != null) {
                        int start = Math.min(lastSelectedIndex, currentIndex);
                        int end = Math.max(lastSelectedIndex, currentIndex);

                        for (int i = start; i <= end; i++) {
                            getTableView().getItems().get(i).setSelected(true);
                        }
                    }
                    lastSelectedIndex = currentIndex;
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (boundTo != null) {
                    checkBox.selectedProperty().unbindBidirectional(boundTo);
                    boundTo = null;
                }

                if (empty) {
                    setGraphic(null);
                    return;
                }

                var rowItem = getTableView().getItems().get(getIndex());
                boundTo = rowItem.selectedProperty();
                checkBox.selectedProperty().bindBidirectional(boundTo);

                setGraphic(checkBox);
            }
        });

        Map<VideoDTO, List<FrameDTO>> groupedFrames = frameQuery.getAll().stream().collect(Collectors.groupingBy(FrameDTO::video));
        videoQuery.getAllVideos().forEach(v -> groupedFrames.computeIfAbsent(v, e -> new ArrayList<>()));

        cachedVideoList = FXCollections.observableArrayList(
                groupedFrames.entrySet().stream().map(e -> new ExportTableEntry(e.getKey(), e.getValue())).toList()
        );
        videoTable.setItems(cachedVideoList);

        exportAccordion.setExpandedPane(videoPane);

        bind(clearButton, "export.button.clear");
        bind(cancelButton, "export.button.cancel");
        bind(searchField, "export.search-prompt");
        bind(exportButton, "export.button.export");

        bind(videoPane, "export.accordion.video");

        bind(videoDataPane, "export.accordion.video-data");
        videoFieldsList.getItems().addAll(
                ChosenVideoAnalytics.getEntries().stream().map(VideoExportActionEntry::new).toList()
        );
        setupList(videoFieldsList);

        bind(tagDataPane, "export.accordion.tag-data");
        tagFieldsList.getItems().addAll(
                ChosenTagAnalytics.getEntries().stream().map(TagExportActionEntry::new).toList()
        );
        setupList(tagFieldsList);

        fileTypeBox.getItems().addAll("Excel", "CSV");

        addKeybinds();

        Platform.runLater(() -> {
            var stage = (Stage) exportView.getScene().getWindow();
            stage.setOnCloseRequest(e -> close());
        });
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
                    checkBox.selectedProperty().unbindBidirectional(boundItem.selectedProperty);
                    boundItem = null;
                }

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    boundItem = (ExportActionEntry) item;
                    checkBox.setText(((ExportActionEntry) item).getLabel());
                    checkBox.selectedProperty().bindBidirectional(((ExportActionEntry) item).selectedProperty);
                    setGraphic(root);
                }
            }
        });
    }

    private List<VideoDTO> getSelectedVideos() {
        return cachedVideoList.stream()
                .filter(ExportTableEntry::isSelected)
                .map(ExportTableEntry::getVideo)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    @FXML
    public void onClear() {
        cachedVideoList.forEach(e -> e.setSelected(false));
    }

    @FXML
    public void handleSearch() {
        SearchUtils.handleSearch(
                searchButton,
                searchField,
                videoTable,
                cachedVideoList,
                (list, query) -> list.filtered(e ->
                        e.getVideo().name().toLowerCase().contains(query.toLowerCase())
                )
        );
    }

    @FXML
    @Async
    public void onExport() {
        if(fileTypeBox.getSelectionModel().getSelectedIndex() == -1) return; //TODO: ERROR

        var videosToExport = getSelectedVideos();
        if (videosToExport.isEmpty()) return;

        var videoExportActions = videoFieldsList.getItems().stream()
                .filter(ExportActionEntry::isSelected)
                .map(VideoExportActionEntry::getAnalytics)
                .collect(Collectors.toCollection(LinkedList::new));
        var tagExportActions = tagFieldsList.getItems().stream()
                .filter(ExportActionEntry::isSelected)
                .map(TagExportActionEntry::getAnalytics)
                .collect(Collectors.toCollection(LinkedList::new));

        var fileFormat = fileTypeBox.getSelectionModel().getSelectedItem();
        try {
            var dir = FileChooserProvider.locationFileSaveChooser(
                        (Stage) tagDataPane.getScene().getWindow(),
                        "CSV".equals(fileFormat) ? ".zip" : ".xlsx",
                        userSettingsAdapter.useRecentExportPath() ? userSettingsAdapter.getRecentExportPath() : ""
                    );

            var fileBytes = "CSV".equals(fileFormat)
                    ? csvExportPort.exportToZipByteArray(new ExportDataInput(null,
                            videosToExport,
                            videoExportActions,
                            tagExportActions
                    ))
                    : excelExportPort.exportToExcelByteArray(new ExportDataInput(null,
                            videosToExport,
                            videoExportActions,
                            tagExportActions
                    ));
            Files.write(Path.of(dir), fileBytes);
        } catch (Exception e) {
            //TODO: error
            e.printStackTrace();
        }
    }

    private void handleWholeSelection() {
        var allSelected = cachedVideoList.stream().allMatch(ExportTableEntry::isSelected);
        cachedVideoList.forEach(e -> e.setSelected(!allSelected));
    }

    @Override
    public void addKeybinds() {
        keyActions.put(new KeyCodeCombination(KeyCode.C, KeyCombination.SHIFT_DOWN), this::close);
        keyActions.put(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN), this::handleWholeSelection);
        keyActions.put(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN), this::handleSearch);

        addEventFilter(exportView);
    }

    @Override
    public void close() {
        VideoDeletedEventDispatcher.unregister(this);
        VideoPathUpdatedEventDispatcher.unregister(this);

        uiManager.close(UIFlag.EXPORT);
        var stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @Override
    public void onDeleteVideo(@NotNull VideoDTO video) {
        var entry = cachedVideoList.stream().filter(e -> e.getVideo().equals(video)).findFirst().orElse(null);
        cachedVideoList.remove(entry);
    }

    @Override
    public void onVideoPathUpdated(@NotNull VideoDTO video) {
        var entry = cachedVideoList.stream().filter(e -> e.getVideo().equals(video)).findFirst().orElse(null);
        entry.setVideo(video);
    }
}
