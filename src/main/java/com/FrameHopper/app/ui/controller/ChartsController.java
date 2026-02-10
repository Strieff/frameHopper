package com.FrameHopper.app.ui.controller;

import com.FrameHopper.app.View.Elements.FXElementsProviders.FXDialogProvider;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXIconLoader;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FileChooserProvider;
import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.adapters.settings.UserSettingsAdapter;
import com.FrameHopper.app.boundry.dto.analytics.VideoDataDTO;
import com.FrameHopper.app.core.application.analytics.VideoAnalyticsQuery;
import com.FrameHopper.app.ui.FXMLViewLoader;
import com.FrameHopper.app.ui.ve.ChartsActionEntry;
import com.FrameHopper.app.core.ports.in.frame.FrameQuery;
import com.FrameHopper.app.core.ports.in.video.VideoQuery;
import com.FrameHopper.app.ui.UiView;
import com.FrameHopper.app.ui.ve.ChartsTableEntry;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class ChartsController implements UiView {
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
    private CheckBox meanCheckbox,colorMean;
    @FXML
    private Button generateButton;
    @FXML
    private TextField tickField,separatorField;
    @FXML
    private Label tickLabel, separatorLabel, yAxisLabel, legend1Label, legend2Label, legend3Label;
    @FXML
    private VBox saveArea;
    @FXML
    private HBox meanArea,legendBox2;
    @FXML
    Rectangle rectangle1,rectangle2;
    @FXML
    ImageView importButtonIcon, exportButtonIcon, clearButtonIcon, saveButtonIcon;

    private final FrameQuery frameQuery;
    private final VideoQuery videoQuery;
    private final VideoAnalyticsQuery videoAnalyticsQuery;
    private final UserSettingsAdapter userSettingsAdapter;

    public ChartsController(
            FrameQuery frameQuery,
            VideoQuery videoQuery,
            VideoAnalyticsQuery videoAnalyticsQuery,
            UserSettingsAdapter userSettingsAdapter
    ) {
        this.frameQuery = frameQuery;
        this.videoQuery = videoQuery;
        this.videoAnalyticsQuery = videoAnalyticsQuery;
        this.userSettingsAdapter = userSettingsAdapter;
    }

    @FXML
    public void initialize() {
        videoTable.setPlaceholder(new Label(Dictionary.get("placeholder.video")));

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

        videoTable.getItems().addAll(entries);
        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));

        nameColumn.setText(Dictionary.get("export.name"));
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
        meanCheckbox.setText(Dictionary.get("checkbox.mean"));

        colorMean.setOnMouseClicked(e -> generateChart());
        colorMean.setText(Dictionary.get("checkbox.color"));

        generateButton.setOnMouseClicked(e -> generateChart());
        generateButton.setText(Dictionary.get("chart.generate"));

        yAxisLabel.setText(Dictionary.get("y-axis.options"));
        yAxisOptions.getItems().addAll(getChartsOptions());
        yAxisOptions.getSelectionModel().select(0);
        yAxisOptions.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
           if(newVal != null)
               generateChart();
        });
        yAxisOptions.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(ChartsActionEntry item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getLabel());
                }
            }
        });
        yAxisOptions.setCellFactory(chartCellFactory);
        yAxisOptions.setButtonCell(chartCellFactory.call(null));

        tickLabel.setText(Dictionary.get("y-axis.ticks"));
        separatorLabel.setText(Dictionary.get("y-axis.separator"));

        exportButtonIcon.setImage(FXIconLoader.getLargeIcon("export.png"));
        importButtonIcon.setImage(FXIconLoader.getLargeIcon("import.png"));
        clearButtonIcon.setImage(FXIconLoader.getLargeIcon("clean.png"));
        saveButtonIcon.setImage(FXIconLoader.getLargeIcon("save.png"));

        var optionLabel = yAxisOptions.getSelectionModel().getSelectedItem().getLabel();
        legend1Label.setText(!colorMean.isSelected() ? optionLabel : String.format(Dictionary.get("legend.green"), optionLabel));
        legend2Label.setText(String.format(Dictionary.get("legend.red"), optionLabel));
        legend3Label.setText(Dictionary.get("legend.mean"));

        Platform.runLater(() -> {
            var stage = (Stage) videoTable.getScene().getWindow();
            stage.setOnCloseRequest(e -> close());
        });
    }

    private final Callback<ListView<ChartsActionEntry>, ListCell<ChartsActionEntry>> chartCellFactory =
            lv -> new ListCell<>() {
                @Override
                protected void updateItem(ChartsActionEntry item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getLabel());
                }
            };

    private List<ChartsActionEntry> getChartsOptions() {
        return List.of(
                new ChartsActionEntry("chart.complexity", data -> data.stream().collect(Collectors.toMap(
                        vd -> vd.video().name(),
                        vd -> videoAnalyticsQuery.getComplexity(vd).data()
                ))),
                new ChartsActionEntry("chart.tags.unique", data -> data.stream().collect(Collectors.toMap(
                        vd -> vd.video().name(),
                        vd -> videoAnalyticsQuery.getUniqueTagsCount(vd).data()
                ))),
                new ChartsActionEntry("chart.frame-count", data -> data.stream().collect(Collectors.toMap(
                        vd -> vd.video().name(),
                   vd -> videoAnalyticsQuery.getFrameCount(vd).data()
                ))),
                new ChartsActionEntry("chart.duration", data -> data.stream().collect(Collectors.toMap(
                        vd -> vd.video().name(),
                        vd -> videoAnalyticsQuery.getRuntime(vd).data()
                ))),
                new ChartsActionEntry("chart.points", data -> data.stream().collect(Collectors.toMap(
                        vd -> vd.video().name(),
                        vd -> videoAnalyticsQuery.getTotalPoints(vd).data()
                )))
        );
    }

    //region [Chart generation]

    private void generateChart() {
        var option = yAxisOptions.getSelectionModel().getSelectedItem();

        meanArea.setVisible(meanCheckbox.isSelected());
        legend1Label.setText(!colorMean.isSelected() ? option.getLabel() : String.format(Dictionary.get("legend.green"), option.getLabel()));
        legend2Label.setText(String.format(Dictionary.get("legend.red"), option.getLabel()));

        if(!colorMean.isSelected()){
            rectangle1.setFill(Color.valueOf("blue"));
            legendBox2.setVisible(false);
        }else{
            rectangle1.setFill(Color.valueOf("green"));
            rectangle2.setFill(Color.valueOf("red"));
            legendBox2.setVisible(true);
        }

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
        return videoTable.getItems().stream()
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

            var loader = FXMLViewLoader.getView(
                    "ImportChartViewModel",
                    "Imported chart",
                    saveArea
            );

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
        videoTable.getItems().forEach(e -> e.setSelected(false));
        generateChart();
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
    public void close() {
        var stage = (Stage) saveArea.getScene().getWindow();
        stage.close();
    }
}
