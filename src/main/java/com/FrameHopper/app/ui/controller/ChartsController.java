package com.FrameHopper.app.ui.controller;

import com.FrameHopper.app.View.Elements.FXElementsProviders.FXDialogProvider;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXIconLoader;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FileChooserProvider;
import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.adapters.settings.UserSettingsAdapter;
import com.FrameHopper.app.boundry.dto.FrameDTO;
import com.FrameHopper.app.boundry.dto.TagDTO;
import com.FrameHopper.app.boundry.dto.VideoDTO;
import com.FrameHopper.app.boundry.dto.analytics.VideoDataDTO;
import com.FrameHopper.app.core.application.analytics.VideoAnalyticsQuery;
import com.FrameHopper.app.ui.UIFlag;
import com.FrameHopper.app.ui.UIManager;
import com.FrameHopper.app.ui.eventing.*;
import com.FrameHopper.app.ui.utils.ChartUtils;
import com.FrameHopper.app.ui.utils.SearchUtils;
import com.FrameHopper.app.ui.ve.ChartsActionEntry;
import com.FrameHopper.app.core.ports.in.frame.FrameQuery;
import com.FrameHopper.app.core.ports.in.video.VideoQuery;
import com.FrameHopper.app.ui.UiView;
import com.FrameHopper.app.ui.ve.ChartsTableEntry;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static javafx.scene.paint.Color.*;

@Component
@Scope("prototype")
public class ChartsController extends UiView implements
        VideoPathUpdatedListener,
        VideoDeletedEventListener,
        TagDeletedEventListener,
        TagUpdatedEventListener,
        FrameUpdatedEventListener
{
    @FXML
    private BorderPane chartView;
    @FXML
    private StackPane chartPane;
    @FXML
    private TableView<ChartsTableEntry> videoTable;
    @FXML
    private TableColumn<ChartsTableEntry, Boolean> selectColumn;
    @FXML
    private TableColumn<ChartsTableEntry, String> nameColumn;
    @FXML
    private ComboBox<ChartsActionEntry> yAxisOptions;
    @FXML
    private CheckBox meanCheckbox, colorMean;
    @FXML
    private Button generateButton, searchButton;
    @FXML
    private TextField tickField, separatorField, searchField;
    @FXML
    private Label tickLabel, separatorLabel, yAxisLabel;
    @FXML
    private VBox saveArea;
    @FXML
    private HBox legendContainer;
    @FXML
    ImageView importButtonIcon, exportButtonIcon, clearButtonIcon, saveButtonIcon;

    private final HBox greenBox, redBox, blueBox, meanBox;

    private final StringProperty chartOptionProperty = new SimpleStringProperty("");

    private final FrameQuery frameQuery;
    private final VideoQuery videoQuery;
    private final VideoAnalyticsQuery videoAnalyticsQuery;
    private final UserSettingsAdapter userSettingsAdapter;
    private final UIManager uiManager;

    private ObservableList<ChartsTableEntry> cachedVideoList;

    public ChartsController(
            FrameQuery frameQuery,
            VideoQuery videoQuery,
            VideoAnalyticsQuery videoAnalyticsQuery,
            UserSettingsAdapter userSettingsAdapter,
            UIManager uiManager
    ) {
        this.frameQuery = frameQuery;
        this.videoQuery = videoQuery;
        this.videoAnalyticsQuery = videoAnalyticsQuery;
        this.userSettingsAdapter = userSettingsAdapter;
        this.uiManager = uiManager;

        var greenLabel = ChartUtils.getLabel(50, 140, "charts.legend.green", chartOptionProperty);
        greenBox = ChartUtils.getLegendBox(greenLabel, 5, 40, GREEN, 100, 200);

        var redLabel = ChartUtils.getLabel(50, 140, "charts.legend.red", chartOptionProperty);
        redBox = ChartUtils.getLegendBox(redLabel, 5, 40, RED, 100, 200);

        var blueLabel = ChartUtils.getLabel(50, 140, "charts.legend.blue", chartOptionProperty);
        blueBox = ChartUtils.getLegendBox(blueLabel, 5, 40, BLUE, 100, 200);

        var meanLabel = ChartUtils.getMeanLabel(50, 140, "charts.legend.mean");
        meanBox = ChartUtils.getMeanLegendBox(meanLabel, ORANGE, 5, 100, 200);

        VideoPathUpdatedEventDispatcher.register(this);
        VideoDeletedEventDispatcher.register(this);
        TagDeletedEventDispatcher.register(this);
        TagUpdatedEventDispatcher.register(this);
        FrameUpdatedEventDispatcher.register(this);
    }

    @FXML
    public void initialize() {
        bind(searchField, "charts.search-prompt");

        cachedVideoList = FXCollections.observableArrayList(cacheData());

        videoTable.setItems(cachedVideoList);
        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));

        bind(nameColumn, "charts.table.name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setCellFactory(c -> new TableCell<>() {
            private final Text text = new Text();

            {
                text.wrappingWidthProperty().bind(c.widthProperty().subtract(10)); // padding
                setGraphic(text);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                text.setText(empty || item == null ? null : item);
            }
        });

        meanCheckbox.setOnMouseClicked(e -> generateChart());
        bind(meanCheckbox, "charts.chart-options.show-mean");

        colorMean.setOnMouseClicked(e -> generateChart());
        bind(colorMean, "charts.chart-options.color-mean");

        generateButton.setOnMouseClicked(e -> generateChart());
        generateButton.setText(Dictionary.get("chart.generate"));
        bind(generateButton, "charts.button.generate");

        bind(yAxisLabel, "charts.y-axis-options.label");
        yAxisOptions.getItems().addAll(getChartsOptions());
        yAxisOptions.getSelectionModel().select(0);
        chartOptionProperty.setValue(yAxisOptions.getSelectionModel().getSelectedItem().getLabel());

        yAxisOptions.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
           if(newVal != null) {
               generateChart();
               chartOptionProperty.setValue(newVal.getLabel());
           }
        });

        yAxisOptions.setCellFactory(chartCellFactory);
        yAxisOptions.setButtonCell(chartCellFactory.call(null));

        bind(tickLabel, "charts.chart-options.ticks");
        bind(separatorLabel, "charts.chart-options.separator");

        exportButtonIcon.setImage(FXIconLoader.getLargeIcon("export.png"));
        importButtonIcon.setImage(FXIconLoader.getLargeIcon("import.png"));
        clearButtonIcon.setImage(FXIconLoader.getLargeIcon("clean.png"));
        saveButtonIcon.setImage(FXIconLoader.getLargeIcon("save.png"));

        legendContainer.setAlignment(Pos.CENTER);
        legendContainer.setSpacing(10);
        ChartUtils.populateTable(legendContainer, greenBox, redBox, meanBox);

        addKeybinds();

        Platform.runLater(() -> {
            var stage = (Stage) chartView.getScene().getWindow();
            stage.setOnCloseRequest(e -> close());
        });
    }

    private final Callback<ListView<ChartsActionEntry>, ListCell<ChartsActionEntry>> chartCellFactory =
            lv -> new ListCell<>() {
                @Override
                protected void updateItem(ChartsActionEntry item, boolean empty) {
                    super.updateItem(item, empty);

                    textProperty().unbind();

                    if (empty || item == null) {
                        setText(null);
                    } else {
                        textProperty().bind(item.labelProperty());
                    }
                }
            };

    private List<ChartsActionEntry> getChartsOptions() {
        return List.of(
                new ChartsActionEntry("charts.y-axis-options.complexity", data -> data.stream().collect(Collectors.toMap(
                        vd -> vd.video().name(),
                        vd -> videoAnalyticsQuery.getComplexity(vd).data()
                ))),
                new ChartsActionEntry("charts.y-axis-options.unique-tags", data -> data.stream().collect(Collectors.toMap(
                        vd -> vd.video().name(),
                        vd -> videoAnalyticsQuery.getUniqueTagsCount(vd).data()
                ))),
                new ChartsActionEntry("charts.y-axis-options.total-frame-count", data -> data.stream().collect(Collectors.toMap(
                        vd -> vd.video().name(),
                   vd -> videoAnalyticsQuery.getFrameCount(vd).data()
                ))),
                new ChartsActionEntry("charts.y-axis-options.duration", data -> data.stream().collect(Collectors.toMap(
                        vd -> vd.video().name(),
                        vd -> videoAnalyticsQuery.getRuntime(vd).data()
                ))),
                new ChartsActionEntry("charts.y-axis-options.total-points", data -> data.stream().collect(Collectors.toMap(
                        vd -> vd.video().name(),
                        vd -> videoAnalyticsQuery.getTotalPoints(vd).data()
                )))
        );
    }

    private List<ChartsTableEntry> cacheData() {
        var videos = videoQuery.getAllVideos();
        var frames = frameQuery.getAll();

        var framesByVideoId = frames.stream()
                .filter(f -> f != null && f.video() != null)
                .collect(Collectors.groupingBy(f -> f.video().id()));

        var entries = videos.stream()
                .map(v -> new ChartsTableEntry(
                        v,
                        framesByVideoId.getOrDefault(v.id(), new ArrayList<>())
                ))
                .toList();

        entries.forEach(e -> e.selectedProperty().addListener((obs, oldVal, newVal) -> generateChart()));

        return entries;
    }

    //region [Chart generation]

    private void generateChart() {
        var option = yAxisOptions.getSelectionModel().getSelectedItem();

        List<HBox> legend = new  ArrayList<>();

        if(colorMean.isSelected())
            legend.addAll(List.of(greenBox, redBox));
        else
            legend.add(blueBox);

        if(meanCheckbox.isSelected())
            legend.add(meanBox);

        ChartUtils.populateTable(legendContainer, legend.toArray(HBox[]::new));

        chartPane.getChildren().clear();

        var selected = getSelectedForAnalytics();
        if(selected.isEmpty()) return;

        var data = option.apply(selected);
        var maxValue = data.values().stream().map(Number::doubleValue).max(Comparator.naturalOrder()).orElse(0d);

        if(!meanCheckbox.isSelected())
            chartPane.getChildren().add(createBarChart(data, maxValue,option.getLabel()));
        else
            chartPane.getChildren().add(layerCharts(
                    createBarChart(data, maxValue,option.getLabel()),
                    createLineChart(data, maxValue,option.getLabel())
            ));
    }

    private NumberAxis createYAxis(double maxValue, String title){
        double separator;
        try{
            separator = Double.parseDouble(separatorField.getText().replace(",","."));
        }catch(NumberFormatException e){
            separator = 10;
        }

        int ticks;
        try{
            ticks = Integer.parseInt(tickField.getText());
        }catch(NumberFormatException e){
            ticks = 10;
        }

        final NumberAxis yAxis = new NumberAxis(0,Math.ceil(maxValue),separator);
        yAxis.setPrefWidth(80);
        yAxis.setMinorTickCount(ticks);
        yAxis.setLabel(title);

        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis){
            @Override
            public String toString(Number value) {
                return String.format("%.2f", value.doubleValue());
            }
        });

        return yAxis;
    }

    private BarChart<String, Number> createBarChart(Map<String,Number> valueMap, Double maxValue, String title) {
        var average = valueMap.values().stream()
                .mapToDouble(Number::doubleValue)
                .summaryStatistics()
                .getAverage();

        final BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), createYAxis(maxValue,title));
        chart.setLegendVisible(false);
        chart.setAnimated(false);

        var dtoList = FXCollections.observableArrayList(
                valueMap.keySet().stream()
                        .map(e -> new XYChart.Data(e, valueMap.get(e)))
                        .toList()
        );

        chart.getData().add(new XYChart.Series(dtoList));
        if(colorMean.isSelected())
            chart.getData().getFirst().getData().forEach(d -> d.getNode().setStyle(
                    d.getYValue().doubleValue()>= average ?
                            "-fx-bar-fill: GREEN" :
                            "-fx-bar-fill: RED"
            ));
        else
            chart.getData().getFirst().getData().forEach(d -> d.getNode().setStyle("-fx-bar-fill: BLUE"));

        return chart;
    }

    private LineChart<String, Number> createLineChart(Map<String,Number> valueMap, Double maxValue, String title){
        final LineChart<String, Number> chart = new LineChart<>(new CategoryAxis(), createYAxis(maxValue,title));
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCreateSymbols(false);

        var mean = valueMap.values().stream()
                .mapToDouble(Number::doubleValue)
                .summaryStatistics()
                .getAverage();;

        var dtoList = FXCollections.observableArrayList(
                valueMap.keySet().stream()
                        .map(e -> new XYChart.Data(e, mean))
                        .toArray()
        );

        chart.getData().addAll(new XYChart.Series(dtoList));

        return chart;
    }

    public StackPane layerCharts(final XYChart<String,Number>... charts){
        for (XYChart<String, Number> chart : charts)
            configureOverlayChart(chart);

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(charts);

        return stackPane;
    }

    private void configureOverlayChart(final XYChart<String, Number> chart) {
        chart.setAlternativeRowFillVisible(false);
        chart.setAlternativeColumnFillVisible(false);
        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalGridLinesVisible(false);
        chart.getXAxis().setVisible(false);
        chart.getYAxis().setVisible(false);

        chart.getStylesheets().addAll(getClass().getClassLoader().getResource("styling/overlay-chart.css").toExternalForm());
    }

    private List<VideoDataDTO> getSelectedForAnalytics() {
        return cachedVideoList.stream()
                .filter(ChartsTableEntry::isSelected)
                .map(e -> new VideoDataDTO(e.getVideo(), e.getFrames()))
                .toList();
    }

    //endregion

    @FXML
    public void handleImport() {
        try {
            var path = FileChooserProvider.textFileChooser((Stage)saveArea.getScene().getWindow());
            var br = new BufferedReader(new FileReader(path));
            var customDataLabel = br.readLine().split(";")[1];
            var customData = br.lines().collect(Collectors.toMap(
                    l -> l.split(";")[0],
                    l -> Double.parseDouble(l.split(";")[1])
            ));

            var loader = uiManager.open(UIFlag.IMPORT_CHARTS, saveArea);

            ImportChartController controller = loader.getController();
            controller.init(
                    customDataLabel,
                    customData,
                    meanCheckbox.isSelected(),
                    colorMean.isSelected(),
                    separatorField.getText(),
                    tickField.getText()
            );
        } catch (Exception e) {

        }
    }

    @Async
    @FXML
    public void handleExport() {
        try {
            var fileDir = com.FrameHopper.app.ui.dialog.FileChooserProvider.locationFileSaveChooser(
                    (Stage) chartPane.getScene().getWindow(),
                    ".csv",
                    userSettingsAdapter.useRecentExportPath() ? userSettingsAdapter.getRecentExportPath() : ""
            );

            var data = getSelectedForAnalytics();
            var option = yAxisOptions.getSelectionModel().getSelectedItem();
            var processedData = option.apply(data);

            final StringBuilder output = new StringBuilder()
                    .append("name;").append(option.getLabel()); //TODO: change to use dictionary

            processedData.forEach((key, value) -> output
                    .append("\n")
                    .append(key)
                    .append(";")
                    .append(value.doubleValue())
            );

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileDir), StandardCharsets.UTF_8));

            writer.write('\uFEFF'); // UTF-8 BOM
            writer.write(output.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            //TODO
        } catch (Exception e) {
            FXDialogProvider.errorDialog(e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleClear() {
        cachedVideoList.forEach(e -> e.setSelected(false));
        generateChart();
    }

    @FXML
    public void handleSearch() {
        SearchUtils.handleSearch(
            searchButton,
            searchField,
            videoTable,
            cachedVideoList,
            (list, query) -> list.filtered(e -> e.getVideo().name().toLowerCase().contains(query.toLowerCase()))
        );
    }

    @Async
    @FXML
    public void handleSave() {
        try {
            var fileDir = com.FrameHopper.app.ui.dialog.FileChooserProvider.locationFileSaveChooser(
                    (Stage)chartPane.getScene().getWindow(),
                    ".png",
                    userSettingsAdapter.useRecentExportPath() ? userSettingsAdapter.getRecentExportPath() : ""
            );

            var snapshot = new WritableImage((int) saveArea.getWidth(),(int) saveArea.getHeight());
            saveArea.snapshot(new SnapshotParameters(), snapshot);

            var file = new File(fileDir);

            ImageIO.write(SwingFXUtils.fromFXImage(snapshot,null),"png",file);
        } catch (IOException e) {
            e.printStackTrace();
            //TODO
        } catch (Exception e){
            FXDialogProvider.errorDialog(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void addKeybinds() {
        keyActions.put(new KeyCodeCombination(KeyCode.C, KeyCombination.SHIFT_DOWN), this::close);
        keyActions.put(new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN), this::handleSave);
        keyActions.put(new KeyCodeCombination(KeyCode.I, KeyCombination.SHIFT_DOWN), this::handleImport);
        keyActions.put(new KeyCodeCombination(KeyCode.E, KeyCombination.SHIFT_DOWN), this::handleExport);
        keyActions.put(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN), this::handleClear);
        keyActions.put(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN), this::handleSearch);

        addEventFilter(chartView);
    }

    @Override
    public void close() {
        VideoPathUpdatedEventDispatcher.unregister(this);
        VideoDeletedEventDispatcher.unregister(this);
        TagDeletedEventDispatcher.unregister(this);
        TagUpdatedEventDispatcher.unregister(this);
        FrameUpdatedEventDispatcher.unregister(this);

        uiManager.close(UIFlag.CHARTS);
        var stage = (Stage) saveArea.getScene().getWindow();
        stage.close();
    }

    @Override
    @Async
    public void onDeleteVideo(@NotNull VideoDTO video) {
        var entry = cachedVideoList.stream().filter(e -> e.getVideo().equals(video)).findFirst().orElse(null);
        cachedVideoList.remove(entry);
        if(entry.isSelected()) generateChart();
    }

    @Override
    @Async
    public void onVideoPathUpdated(@NotNull VideoDTO video) {
        var entry = cachedVideoList.stream().filter(e -> e.getVideo().equals(video)).findFirst().orElse(null);
        entry.setVideo(video);
        if(entry.isSelected()) generateChart();
    }

    @Override
    @Async
    public void onFrameUpdate(int frameNumber, FrameDTO frame) {
        if(frame == null && cachedVideoList.stream().noneMatch(ChartsTableEntry::isSelected)) return;

        cachedVideoList.clear();
        cachedVideoList.addAll(cacheData());
    }

    @Override
    @Async
    public void onTagDeleted(@NotNull TagDTO tag) {
        if(cachedVideoList.stream().noneMatch(ChartsTableEntry::isSelected)) return;

        generateChart();
    }

    @Override
    @Async
    public void onTagDeleted(@NotNull List<TagDTO> tags) {
        if(cachedVideoList.stream().noneMatch(ChartsTableEntry::isSelected)) return;

        generateChart();
    }

    @Override
    @Async
    public void onTagUpdated(@NotNull TagDTO tag) {
        if(cachedVideoList.stream().noneMatch(ChartsTableEntry::isSelected)) return;

        generateChart();
    }

    @Override
    @Async
    public void onTagUpdated(@NotNull List<TagDTO> tags) {
        if(cachedVideoList.stream().noneMatch(ChartsTableEntry::isSelected)) return;

        generateChart();
    }
}
