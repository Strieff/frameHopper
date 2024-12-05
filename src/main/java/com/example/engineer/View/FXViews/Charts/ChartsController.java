package com.example.engineer.View.FXViews.Charts;

import com.example.engineer.View.Elements.DataManagers.OpenViewsInformationContainer;
import com.example.engineer.View.Elements.DataManagers.UserSettingsManager;
import com.example.engineer.View.Elements.FXElementsProviders.FXDialogProvider;
import com.example.engineer.View.Elements.FXElementsProviders.FXIconLoader;
import com.example.engineer.View.Elements.FXElementsProviders.FXMLViewLoader;
import com.example.engineer.View.Elements.FXElementsProviders.FileChooserProvider;
import com.example.engineer.View.Elements.Language.Dictionary;
import com.example.engineer.View.Elements.Language.LanguageChangeListener;
import com.example.engineer.View.Elements.Language.LanguageManager;
import jakarta.annotation.PostConstruct;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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
public class ChartsController implements LanguageChangeListener {
    @FXML
    BorderPane chartView;
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
    private Button generateButton;
    @FXML
    private TextField tickField,separatorField;
    @FXML
    private Label tickLabel,separatorLabel,yAxisLabel, legend1Label, legend2Label, legend3Label;
    @FXML
    private VBox saveArea;
    @FXML
    private HBox meanArea,legendBox2;
    @FXML
    Rectangle rectangle1,rectangle2;
    @FXML
    ImageView importButtonIcon,exportButtonIcon,clearButtonIcon,saveButtonIcon;

    @Autowired
    ChartsService viewService;
    @Autowired
    OpenViewsInformationContainer openViews;
    @Autowired
    UserSettingsManager userSettings;

    private final List<TableEntry> entries = new ArrayList<>();
    private final Map<KeyCombination,Runnable> keyActions = new HashMap<>();

    @PostConstruct
    public void init() {
         viewService.loadData();
    }

    @FXML
    public void initialize(){
        LanguageManager.register(this);

        videoTable.setItems(viewService.getVideos());
        videoTable.setPlaceholder(new Label(Dictionary.get("placeholder.video")));

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
        nameColumn.setText(Dictionary.get("export.name"));

        meanCheckbox.setOnMouseClicked(e -> generateChart());
        meanCheckbox.setText(Dictionary.get("checkbox.mean"));
        colorMean.setOnMouseClicked(e -> generateChart());
        colorMean.setText(Dictionary.get("checkbox.color"));
        generateButton.setOnMouseClicked(e -> generateChart());
        generateButton.setText(Dictionary.get("chart.generate"));

        yAxisLabel.setText(Dictionary.get("y-axis.options"));
        yAxisOptions.getItems().addAll(getChartOptions());
        yAxisOptions.getSelectionModel().select(0);
        yAxisOptions.getSelectionModel().selectedItemProperty().addListener((observable,oldValue,newValue) -> {
            if(newValue!=null)
                generateChart();
        });

        tickLabel.setText(Dictionary.get("y-axis.ticks"));
        separatorLabel.setText(Dictionary.get("y-axis.separator"));

        exportButtonIcon.setImage(FXIconLoader.getLargeIcon("export.png"));
        importButtonIcon.setImage(FXIconLoader.getLargeIcon("import.png"));
        clearButtonIcon.setImage(FXIconLoader.getLargeIcon("clean.png"));
        saveButtonIcon.setImage(FXIconLoader.getLargeIcon("save.png"));

        var option = getOption();
        legend1Label.setText(!colorMean.isSelected() ? option : String.format(Dictionary.get("legend.green"),option));
        legend2Label.setText(String.format(Dictionary.get("legend.red"),option));
        legend3Label.setText(Dictionary.get("legend.mean"));

        //add key binds
        keyActions.put(new KeyCodeCombination(KeyCode.C, KeyCombination.SHIFT_DOWN), this::close);
        chartView.addEventFilter(KeyEvent.KEY_PRESSED,this::handleKeyPressed);


        Platform.runLater(() -> {
            var stage = (Stage) videoTable.getScene().getWindow();
            stage.setOnCloseRequest(e -> {
                openViews.closeCharts();
                LanguageManager.unregister(this);
            });
        });
    }

    //HANDLE KEY BINDS
    private void handleKeyPressed(KeyEvent event){
        keyActions.keySet().stream()
                .filter(k -> k.match(event))
                .findFirst()
                .ifPresent(k -> keyActions.get(k).run());
    }

    private void generateChart(){
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
            chartPane.getChildren().add(createBarChart(valueMap, maxValue,option));
        else
            chartPane.getChildren().add(viewService.layerCharts(
                    createBarChart(valueMap, maxValue,option),
                    createLineChart(valueMap, maxValue,option)
            ));

        if(!colorMean.isSelected()){
            rectangle1.setFill(Color.valueOf("blue"));
            legendBox2.setVisible(false);
        }else{
            rectangle1.setFill(Color.valueOf("green"));
            rectangle2.setFill(Color.valueOf("red"));
            legendBox2.setVisible(true);
        }

        meanArea.setVisible(meanCheckbox.isSelected());

        legend1Label.setText(!colorMean.isSelected() ? getOption() : String.format(Dictionary.get("legend.green"),getOption()));

    }

    private NumberAxis createYAxis(double maxValue,String title){
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

    private BarChart<String, Number> createBarChart(Map<String,Number> valueMap,Double maxValue,String title) {
        var mean = viewService.getMean(valueMap);

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
            chart.getData().get(0).getData().forEach(d -> d.getNode().setStyle(
                    d.getYValue().doubleValue()>=mean?
                            "-fx-bar-fill: GREEN":
                            "-fx-bar-fill: RED"
            ));
        else
            chart.getData().get(0).getData().forEach(d -> d.getNode().setStyle("-fx-bar-fill: BLUE"));

        return chart;
    }

    private LineChart<String, Number> createLineChart(Map<String,Number> valueMap,Double maxValue,String title){
        final LineChart<String, Number> chart = new LineChart<>(new CategoryAxis(), createYAxis(maxValue,title));
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

    private void close(){
        openViews.closeCharts();
        LanguageManager.unregister(this);
        var stage = (Stage) saveArea.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleExport(){
        var dir = FileChooserProvider.locationFileChooser((Stage)videoTable.getScene().getWindow(), userSettings.getExportPath());
        String name = FXDialogProvider.inputDialog();
        if(name == null){
            FXDialogProvider.errorDialog(Dictionary.get("error.export.no-name"));
            return;
        }

        while(new File(dir + File.separator + name+ ".csv").exists()){
            var res = FXDialogProvider.customDialog(
                    Dictionary.get("dialog.export.exists"),
                    0,
                    Dictionary.get("cancel"),
                    Dictionary.get("dialog.export.option.rename"),
                    Dictionary.get("dialog.export.option.overwrite")
            );

            switch (res) {
                case 0:
                    FXDialogProvider.messageDialog(Dictionary.get("cancelled"));
                    break;
                case 1:
                    name = FXDialogProvider.inputDialog();
                    if(name.isBlank()) FXDialogProvider.errorDialog(Dictionary.get("error.export.no-name"));
                    break;
            }

            if(res == 2) break;
        }

        var path = dir + File.separator + name + ".csv";

        viewService.export(
                yAxisOptions.getSelectionModel().getSelectedItem(),
                entries.stream().map(d -> d.id).toList(),
                path
        );
    }

    @FXML
    private void handleImport() {
        try{
            var path = FileChooserProvider.textFileChooser((Stage)saveArea.getScene().getWindow());

            var loader = FXMLViewLoader.getView("ImportChartViewModel");

            Parent root = loader.load();
            var importScene = new Scene(root);

            ImportChartController controller = loader.getController();
            controller.init(
                    path,
                    meanCheckbox.isSelected(),
                    colorMean.isSelected(),
                    separatorField.getText(),
                    tickField.getText()
            );

            var secondaryStage = new Stage();
            secondaryStage.setScene(importScene);
            secondaryStage.setTitle("Imported chart");

            secondaryStage.initOwner(saveArea.getScene().getWindow());
            secondaryStage.show();
        }catch (Exception e){
            //throw new RuntimeException(e);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSave(){
        var dir = FileChooserProvider.locationFileChooser((Stage)videoTable.getScene().getWindow(), userSettings.getExportPath());
        String name = FXDialogProvider.inputDialog();
        if(name == null){
            FXDialogProvider.errorDialog(Dictionary.get("error.export.no-name"));
            return;
        }

        while(new File(dir + File.separator + name + ".png").exists()){
            var res = FXDialogProvider.customDialog(
                    Dictionary.get("dialog.export.exists"),
                    0,
                    Dictionary.get("cancel"),
                    Dictionary.get("dialog.export.option.rename"),
                    Dictionary.get("dialog.export.option.overwrite")
            );

            switch (res) {
                case 0:
                    FXDialogProvider.messageDialog(Dictionary.get("cancelled"));
                    break;
                case 1:
                    name = FXDialogProvider.inputDialog();
                    if(name.isBlank()) FXDialogProvider.errorDialog(Dictionary.get("error.export.no-name"));
                    break;
            }

            if(res == 2) break;
        }

        var filePath = dir + File.separator + name + ".png";

        viewService.saveChartAsFile(saveArea,filePath);

        FXDialogProvider.messageDialog(Dictionary.get("file.saved"));
    }

    @FXML
    private void handleClear(){
        videoTable.getItems().forEach(item -> item.setSelected(false));
        entries.clear();
        generateChart();
    }

    @Override
    public void changeLanguage() {
        //change yAxis options
        int selected = yAxisOptions.getSelectionModel().getSelectedIndex();
        yAxisOptions.getItems().clear();
        yAxisOptions.getItems().addAll(getChartOptions());
        yAxisOptions.getSelectionModel().select(selected);
        generateChart();

        videoTable.setPlaceholder(new Label(Dictionary.get("placeholder.video")));
        nameColumn.setText(Dictionary.get("export.name"));

        var option = getOption();
        legend1Label.setText(!colorMean.isSelected() ? option : String.format(Dictionary.get("legend.green"),option));
        legend2Label.setText(String.format(Dictionary.get("legend.red"),option));
        legend3Label.setText(Dictionary.get("legend.mean"));

        yAxisLabel.setText(Dictionary.get("y-axis.options"));
        tickLabel.setText(Dictionary.get("y-axis.ticks"));
        separatorLabel.setText(Dictionary.get("y-axis.separator"));

        meanCheckbox.setText(Dictionary.get("checkbox.mean"));
        colorMean.setText(Dictionary.get("checkbox.color"));

        generateButton.setText(Dictionary.get("chart.generate"));
    }

    private String getOption(){
        var option = yAxisOptions.getSelectionModel().getSelectedItem().toLowerCase();
        return option.replaceFirst(
                String.valueOf(option.charAt(0)),
                String.valueOf(Character.toUpperCase(option.charAt(0)))
        );
    }
}
