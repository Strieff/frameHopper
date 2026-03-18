package com.FrameHopper.app.ui.controller;

import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.adapters.DataExportAdapter;
import com.FrameHopper.app.adapters.settings.UserSettingsAdapter;
import com.FrameHopper.app.boundry.dto.FrameDTO;
import com.FrameHopper.app.boundry.dto.VideoDTO;
import com.FrameHopper.app.boundry.dto.analytics.VideoDataAnalyticsDTO;
import com.FrameHopper.app.boundry.dto.analytics.VideoDataDTO;
import com.FrameHopper.app.core.application.analytics.TagAnalyticsQuery;
import com.FrameHopper.app.core.ports.in.frame.FrameQuery;
import com.FrameHopper.app.ui.UIFlag;
import com.FrameHopper.app.ui.UIManager;
import com.FrameHopper.app.ui.dialog.FileChooserProvider;
import com.FrameHopper.app.ui.ve.ExportActionEntry;
import com.FrameHopper.app.core.application.analytics.VideoAnalyticsQuery;
import com.FrameHopper.app.ui.UiView;
import com.FrameHopper.app.ui.ve.ExportTableEntry;
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
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class ExportController extends UiView {
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
    private ListView<ExportActionEntry.VideoExportActionEntry> videoFieldsList;
    @FXML
    private ListView<ExportActionEntry.TagExportActionEntry> tagFieldsList;
    @FXML
    private TitledPane videoPane, videoDataPane, tagDataPane;
    @FXML
    private Accordion exportAccordion;
    @FXML
    private ComboBox<String> fileTypeBox;

    private final FrameQuery frameQuery;
    private final VideoAnalyticsQuery videoAnalyticsQuery;
    private final TagAnalyticsQuery tagAnalyticsQuery;
    private final UserSettingsAdapter userSettingsAdapter;
    private final DataExportAdapter dataExportAdapter;
    private final UIManager uiManager;

    private Integer lastSelectedIndex = null;

    private ObservableList<ExportTableEntry> cachedVideoList;

    public ExportController(
            FrameQuery frameQuery,
            VideoAnalyticsQuery videoAnalyticsQuery,
            TagAnalyticsQuery tagAnalyticsQuery,
            UserSettingsAdapter userSettingsAdapter,
            DataExportAdapter dataExportAdapter,
            UIManager uiManager
    ) {
        this.frameQuery = frameQuery;
        this.videoAnalyticsQuery = videoAnalyticsQuery;
        this.tagAnalyticsQuery = tagAnalyticsQuery;
        this.userSettingsAdapter = userSettingsAdapter;
        this.dataExportAdapter = dataExportAdapter;
        this.uiManager = uiManager;
    }

    @FXML
    public void initialize() {
        videoNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        videoNameColumn.setText(Dictionary.get("export.name"));
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
        cachedVideoList = FXCollections.observableArrayList(
                groupedFrames.entrySet().stream().map(e -> new ExportTableEntry(e.getKey(), e.getValue())).toList()
        );
        videoTable.setItems(cachedVideoList);

        exportAccordion.setExpandedPane(videoPane);

        clearButton.setText(Dictionary.get("export.clear"));
        cancelButton.setText(Dictionary.get("cancel"));
        searchField.setPromptText(Dictionary.get("search"));
        exportButton.setText(Dictionary.get("export.export"));
        videoPane.setText(Dictionary.get("export.name"));

        videoDataPane.setText(Dictionary.get("export.data.video"));
        setUpVideoOptions();
        setupList(videoFieldsList);
        setUpTagOptions();
        setupList(tagFieldsList);

        fileTypeBox.getItems().addAll("Excel", "CSV");

        addKeybinds();

        Platform.runLater(() -> {
            var stage = (Stage) exportView.getScene().getWindow();
            stage.setOnCloseRequest(e -> close());
        });
    }

    //region [Set Up Export Actions]

    private void setUpVideoOptions() {
        videoFieldsList.getItems().addAll(
                new ExportActionEntry.VideoExportActionEntry(
                        "data.overview.frameCount", "data.overview.summary.frameCount",
                        v -> videoAnalyticsQuery.getFrameCount(v).data(),
                        VideoDataAnalyticsDTO::totalFrameAmount
                ),
                new ExportActionEntry.VideoExportActionEntry(
                        "data.overview.codes", "undefinedCodes",
                        v -> videoAnalyticsQuery.getUniqueTagsCount(v).data(),
                        null
                ),
                new ExportActionEntry.VideoExportActionEntry(
                        "data.overview.runtime", "data.overview.summary.runtime",
                        v -> videoAnalyticsQuery.getRuntime(v).data(),
                        VideoDataAnalyticsDTO::totalRuntime
                ),
                new ExportActionEntry.VideoExportActionEntry("data.overview.framerate", "undefinedFramerate",
                        v -> videoAnalyticsQuery.getFrameRate(v).data(),
                        null
                ),
                new ExportActionEntry.VideoExportActionEntry("data.overview.totalPoints", "data.overview.summary.totalPoints",
                        v -> videoAnalyticsQuery.getTotalPoints(v).data(),
                        VideoDataAnalyticsDTO::totalPoints
                ),
                new ExportActionEntry.VideoExportActionEntry("data.overview.complexity", "data.overview.summary.complexity",
                        v -> videoAnalyticsQuery.getComplexity(v).data(),
                        VideoDataAnalyticsDTO::overallComplexity
                )
        );
    }

    private void setUpTagOptions() {
        tagFieldsList.getItems().addAll(
                new ExportActionEntry.TagExportActionEntry("data.tags.value",
                        t -> tagAnalyticsQuery.getValue(t.tag()).data()
                ),
                new ExportActionEntry.TagExportActionEntry("data.tags.amount",
                        t -> tagAnalyticsQuery.getAmountUsed(t.tag(), t.videos()).data()
                ),
                new ExportActionEntry.TagExportActionEntry("data.tags.totalPoints",
                        t -> tagAnalyticsQuery.getTotalPoints(t.tag(), t.videos()).data()
                )
        );
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
                    checkBox.selectedProperty().unbindBidirectional(boundItem.selectedProperty());
                    boundItem = null;
                }

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    boundItem = (ExportActionEntry) item;
                    checkBox.setText(Dictionary.get(((ExportActionEntry) item).getLabelName()));
                    checkBox.selectedProperty().bindBidirectional(((ExportActionEntry) item).selectedProperty());
                    setGraphic(root);
                }
            }
        });
    }

    //endregion

    private List<VideoDataDTO> getSelectedVideos() {
        return cachedVideoList.stream()
                .filter(ExportTableEntry::isSelected)
                .map(e -> new VideoDataDTO(e.getVideo(), e.getFrames()))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    @FXML
    public void onClear() {
        cachedVideoList.forEach(e -> e.setSelected(false));
    }

    @FXML
    public void handleSearch() {
        var query = searchField.getText().trim();
        if(query.isEmpty() && !searchButton.getText().equals("X")) return;

        if(searchButton.getText().equals("\uD83D\uDD0D")) {
            videoTable.setItems(cachedVideoList.filtered(e -> e.getVideo().name().toLowerCase().contains(query.toLowerCase())));
            searchButton.setText("X");
        } else {
            videoTable.setItems(cachedVideoList);
            searchButton.setText("\uD83D\uDD0D");
            searchField.clear();
        }
    }

    @FXML
    @Async
    public void onExport() {
        if(fileTypeBox.getSelectionModel().getSelectedIndex() == -1) return; //TODO: ERROR

        var videosToExport = getSelectedVideos();

        if (videosToExport.isEmpty()) return;

        var videoExportActions = videoFieldsList.getItems().stream()
                .filter(ExportActionEntry::isSelected)
                .collect(Collectors.toCollection(LinkedList::new));
        var tagExportActions = tagFieldsList.getItems().stream()
                .filter(ExportActionEntry::isSelected)
                .collect(Collectors.toCollection(LinkedList::new));

        //get export data
        Map<String, Map<String, Number>> videoExportData = getVideoExportData(videosToExport, videoExportActions);
        Map<String, Number> videoSummaryExportData = getVideoSummaryExportData(videosToExport, videoExportActions);
        Map<String, Map<String, Number>> tagExportData = getTagExportData(videosToExport, tagExportActions);

        var fileFormat = fileTypeBox.getSelectionModel().getSelectedItem();
        try {
            var dir = "CSV".equals(fileFormat) ?
                    FileChooserProvider.locationWithNameChooser(
                            (Stage) tagDataPane.getScene().getWindow(),
                            userSettingsAdapter.useRecentExportPath() ? userSettingsAdapter.getRecentExportPath() : ""
                    ) : FileChooserProvider.locationFileSaveChooser(
                            (Stage) tagDataPane.getScene().getWindow(),
                            ".xlsx",
                            userSettingsAdapter.useRecentExportPath() ? userSettingsAdapter.getRecentExportPath() : ""
                    );

            if("CSV".equals(fileFormat))
                dataExportAdapter.exportToCSV(videoExportData, videoSummaryExportData, tagExportData, dir);
            else
                dataExportAdapter.exportToExcel(videoExportData, videoSummaryExportData, tagExportData, dir);

        } catch (Exception e) {
            //TODO: error
            e.printStackTrace();
        }
    }

    //region [Export Data]

    private Map<String, Map<String, Number>> getVideoExportData(
            List<VideoDataDTO> videosToExport,
            List<ExportActionEntry.VideoExportActionEntry> videoExportActions
    ) {
        Map<String, Map<String, Number>> videoExportData = new LinkedHashMap<>();
        videosToExport.forEach(e -> {
            Map<String, Number> videoInfo =  new LinkedHashMap<>();

            videoExportActions.forEach(a -> videoInfo.put(a.getLabel(), a.apply(e)));

            videoExportData.put(e.video().name(), videoInfo);
        });

        return videoExportData;
    }

    private Map<String, Number> getVideoSummaryExportData(
            List<VideoDataDTO> videosToExport,
            List<ExportActionEntry.VideoExportActionEntry> videoExportActions
    ) {
        var analytics = videoAnalyticsQuery.getAnalytics(videosToExport);
        Map<String, Number> videoSummaryExportData = new LinkedHashMap<>();
        videoSummaryExportData.put(Dictionary.get("data.overview.summary.totalShotAmount"), analytics.totalShotAmount());
        videoExportActions.forEach(a -> {
            if(!a.hasSummaryAction())
                videoSummaryExportData.put(a.getSummaryLabelName(), null);
            else
                videoSummaryExportData.put(a.getSummaryLabel(), a.summary(analytics).doubleValue());
        });
        videoSummaryExportData.put(Dictionary.get("data.overview.asl"), analytics.asl());

        return videoSummaryExportData;
    }

    private Map<String, Map<String, Number>> getTagExportData(
            List<VideoDataDTO> videosToExport,
            List<ExportActionEntry.TagExportActionEntry> tagExportActions
    ) {
        Map<String, Map<String, Number>> tagExportData = new LinkedHashMap<>();

        if(!tagExportActions.isEmpty())
            frameQuery.getAll().stream()
                    .map(FrameDTO::tags)
                    .flatMap(List::stream)
                    .collect(Collectors.toSet())
                    .forEach(t -> {
                        var tagInfo = new LinkedHashMap<String, Number>();
                        tagExportActions.forEach(a -> tagInfo.put(
                                a.getLabel(),
                                a.apply(new ExportActionEntry.TagExportActionEntry.TagExportDto(t, videosToExport))
                        ));
                        tagExportData.put(t.getName(), tagInfo);
                    });

        return tagExportData;
    }

    //endregion

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
        uiManager.close(UIFlag.EXPORT);
        var stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
