package com.example.engineer.View.FXViews.Charts;

import com.example.engineer.View.Elements.DataManagers.OpenViewsInformationContainer;
import com.example.engineer.View.Elements.Language.Dictionary;
import com.example.engineer.View.Elements.Language.LanguageChangeListener;
import com.example.engineer.View.Elements.Language.LanguageManager;
import jakarta.annotation.PostConstruct;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope("prototype")
public class ChartsController implements LanguageChangeListener {
    @FXML
    private StackPane chartPane;
    @FXML
    private TableView<TableEntry> videoTable;
    @FXML
    private TableColumn<TableEntry, Boolean> selectColumn;
    @FXML
    private TableColumn<TableEntry, String> nameColumn;
    @FXML
    private ComboBox<String> yAxisOptions;
    @FXML
    private CheckBox meanCheckbox,colorMean;
    @FXML
    private Button generateButton,importButton,exportButton,clearButton,saveButton;
    @FXML
    private TextField tickField,separatorField;
    @FXML
    private Label tickLabel,separatorLabel;

    @Autowired
    ChartsService viewService;
    @Autowired
    OpenViewsInformationContainer openViews;

    private List<TableEntry> entries = new ArrayList<>();

    @PostConstruct
    public void init() {
        viewService.loadData();
    }

    @FXML
    public void initialize(){
        LanguageManager.register(this);

        videoTable.setItems(viewService.getVideos());

        //checkbox column
        selectColumn.setCellValueFactory(cd -> cd.getValue().selectedProperty());
        selectColumn.setCellFactory(tc -> new TableCell<>(){
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnMouseClicked(e -> {
                    var currentIndex = getIndex();
                    var entry = getTableView().getItems().get(currentIndex);
                    entry.setSelected(checkBox.isSelected());

                    if(checkBox.isSelected())
                        entries.add(entry);
                    else
                        entries.remove(entry);

                    generateChart();
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if(empty)
                    setGraphic(null);
                else{
                    checkBox.setSelected(item != null && item);
                    setGraphic(checkBox);
                }
            }

        });

        //name column
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<TableEntry, String> call(TableColumn<TableEntry, String> param) {
                return new TableCell<>() {
                    private final Text text = new Text();

                    {
                        text.wrappingWidthProperty().bind(nameColumn.widthProperty());
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

        meanCheckbox.setOnMouseClicked(e -> generateChart());
        colorMean.setOnMouseClicked(e -> generateChart());
        generateButton.setOnMouseClicked(e -> generateChart());

        yAxisOptions.getItems().addAll(getChartOptions());
        yAxisOptions.getSelectionModel().select(0);
        yAxisOptions.getSelectionModel().selectedItemProperty().addListener((observable,oldValue,newValue) -> {
            if(newValue!=null)
                generateChart();
        });


        clearButton.setOnAction(e -> {
            videoTable.getItems().forEach(item -> item.setSelected(false));
            entries.clear();
            generateChart();
        });



        Platform.runLater(() -> {
            var stage = (Stage) videoTable.getScene().getWindow();
            stage.setOnCloseRequest(e -> {
                openViews.closeCharts();
                LanguageManager.unregister(this);
            });
        });
    }

    private  void generateChart(){
        chartPane.getChildren().clear();

        if(entries.isEmpty()) return;

        Map<String,Number> valueMap;
        var ids = entries.stream().map(e -> e.id).toList();

        var option = yAxisOptions.getSelectionModel().getSelectedItem();
        if(Dictionary.get("chart.complexity").equals(option)){
            valueMap = viewService.getComplexity(ids);
        }else if(Dictionary.get("chart.tags.unique").equals(option)){
            valueMap = viewService.getUniqueTagsCount(ids);
        }else if(Dictionary.get("chart.frame-count").equals(option)){
            valueMap = viewService.getFrameCount(ids);
        }else if(Dictionary.get("chart.duration").equals(option)){
            valueMap = viewService.getRuntimes(ids);
        }else if(Dictionary.get("chart.points").equals(option)){
            valueMap = viewService.getTotalPoints(ids);
        }else{
            valueMap = viewService.getComplexity(entries.stream().map(e -> e.id).toList());
        }

        var maxValue = valueMap.values().stream().map(Number::doubleValue).max(Comparator.naturalOrder()).get();

        if(!meanCheckbox.isSelected())
            chartPane.getChildren().add(createBarChart(valueMap,maxValue));
        else
            chartPane.getChildren().add(layerCharts(
                    createBarChart(valueMap,maxValue),
                    createLineChart(valueMap,maxValue)
            ));
    }

    private NumberAxis createYAxis(double maxValue){
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
        yAxis.setLabel("TEST");

        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis){
            @Override
            public String toString(Number value) {
                return String.format("%.2f", value.doubleValue());
            }
        });

        return yAxis;
    }

    private BarChart<String, Number> createBarChart(Map<String,Number> valueMap,Double maxValue) {
        var mean = viewService.getMean(valueMap);

        final BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), createYAxis(maxValue));
        chart.setLegendVisible(false);
        chart.setAnimated(false);

        var dtoList = FXCollections.observableArrayList(
                valueMap.keySet().stream()
                        .map(e -> new XYChart.Data(e, valueMap.get(e)))
                        .toList()
        );

        chart.getData().add(new XYChart.Series(dtoList));
        if(colorMean.isSelected())
            chart.getData().get(0).getData().forEach(d -> d.getNode().setStyle(
                    d.getYValue().doubleValue()>=mean?
                            "-fx-bar-fill: GREEN":
                            "-fx-bar-fill: RED"
            ));

        return chart;
    }

    private LineChart<String, Number> createLineChart(Map<String,Number> valueMap,Double maxValue){
        final LineChart<String, Number> chart = new LineChart<>(new CategoryAxis(), createYAxis(maxValue));
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCreateSymbols(false);


        var mean = viewService.getMean(valueMap);

        var dtoList = FXCollections.observableArrayList(
                valueMap.keySet().stream()
                        .map(e -> new XYChart.Data(e, mean))
                        .toArray()
        );

        chart.getData().addAll(new XYChart.Series(dtoList));

        return chart;
    }

    private StackPane layerCharts(final XYChart<String,Number>... charts){
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

        chart.getStylesheets().addAll(getClass().getClassLoader().getResource("overlay-chart.css").toExternalForm());
    }

    private List<String> getChartOptions(){
        return Arrays.stream(new String[]{
                        "chart.complexity",
                        "chart.tags.unique",
                        "chart.frame-count",
                        "chart.duration",
                        "chart.points"
                })
                .map(Dictionary::get)
                .toList();
    }

    @Override
    public void changeLanguage() {
        //TODO: change languages
    }
}
