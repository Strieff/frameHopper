package com.FrameHopper.app.ui.controller;

import com.FrameHopper.app.View.Elements.FXElementsProviders.FXDialogProvider;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXIconLoader;
import com.FrameHopper.app.ui.settings.UserSettingsAdapter;
import com.FrameHopper.app.ui.UiView;
import com.FrameHopper.app.ui.utils.ChartUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static javafx.scene.paint.Color.*;
import static javafx.scene.paint.Color.ORANGE;

@Component
@Scope("prototype")
public class ImportChartController extends UiView {
    @FXML
    StackPane chartPane;
    @FXML
    Axis<Number> yAxis, yAxisLine;
    @FXML
    ImageView saveButtonIcon, closeButtonIcon;
    @FXML
    VBox saveArea;
    @FXML
    HBox legendContainer;

    private final UserSettingsAdapter userSettingsAdapter;

    public ImportChartController(UserSettingsAdapter userSettingsAdapter) {
        this.userSettingsAdapter = userSettingsAdapter;
    }

    @FXML
    public void initialize(){
        saveButtonIcon.setImage(FXIconLoader.getLargeIcon("save.png"));
        closeButtonIcon.setImage(FXIconLoader.getLargeIcon("x.png"));

        addKeybinds();

        Platform.runLater(() -> {
            var stage = (Stage)chartPane.getScene().getWindow();
            stage.setOnCloseRequest(e -> close());
        });
    }

    public void init(
            String customDataLabel,
            Map<String, Double> customData,
            boolean showMean,
            boolean colorMean,
            String separator,
            String ticks
    ) {
        StringProperty customDataProperty = new SimpleStringProperty(customDataLabel);

        var greenLabel = ChartUtils.getLabel(50, 140, "charts.legend.green", customDataProperty);
        HBox greenBox = ChartUtils.getLegendBox(greenLabel, 5, 40, GREEN, 100, 200);

        var redLabel = ChartUtils.getLabel(50, 140, "charts.legend.red", customDataProperty);
        HBox redBox = ChartUtils.getLegendBox(redLabel, 5, 40, RED, 100, 200);

        var blueLabel = ChartUtils.getLabel(50, 140, "charts.legend.blue", customDataProperty);
        HBox blueBox = ChartUtils.getLegendBox(blueLabel, 5, 40, BLUE, 100, 200);

        var meanLabel = ChartUtils.getMeanLabel(50, 140, "charts.legend.mean");
        HBox meanBox = ChartUtils.getMeanLegendBox(meanLabel, ORANGE, 5, 100, 200);

        List<HBox> legend = new  ArrayList<>();

        if(colorMean)
            legend.addAll(List.of(greenBox, redBox));
        else
            legend.add(blueBox);

        if(showMean)
            legend.add(meanBox);

        ChartUtils.populateTable(legendContainer, legend.toArray(new HBox[0]));

        chartPane.getChildren().clear();
        var maxValue = customData.values().stream().mapToDouble(Number::doubleValue).summaryStatistics().getMax();
        var average = customData.values().stream().mapToDouble(Number::doubleValue).summaryStatistics().getAverage();

        List<XYChart.Data<String, Number>> initialData = new ArrayList<>();
        customData.forEach((key, value) -> initialData.add(new XYChart.Data<>(key, value)));
        var data = FXCollections.observableArrayList(initialData);

        if (!showMean)
            chartPane.getChildren().add(createBarChart(data, maxValue, average, customDataLabel, colorMean, separator, ticks));
        else
            chartPane.getChildren().add(layerCharts(createBarChart(
                            data, maxValue, average, customDataLabel, colorMean, separator, ticks),
                    createLineChart(data, maxValue, average, customDataLabel, separator, ticks)
            ));
    }

    //region [Chart generation]

    private BarChart<String, Number> createBarChart(
            ObservableList<XYChart.Data<String, Number>> data,
            Double maxValue,
            Double average,
            String title,
            boolean colorMean,
            String separator,
            String ticks
    ) {
        final BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), createYAxis(maxValue, title, separator, ticks));
        chart.setLegendVisible(false);
        chart.setAnimated(false);

        chart.getData().add(new XYChart.Series(data));
        if(colorMean)
            chart.getData().getFirst().getData().forEach(d -> d.getNode().setStyle(
                    d.getYValue().doubleValue()>=average?
                            "-fx-bar-fill: GREEN":
                            "-fx-bar-fill: RED"
            ));
        else
            chart.getData().getFirst().getData().forEach(d -> d.getNode().setStyle("-fx-bar-fill: BLUE"));

        return chart;
    }

    private LineChart<String, Number> createLineChart(
            ObservableList<XYChart.Data<String, Number>> data,
            Double maxValue,
            Double average,
            String title,
            String separator,
            String ticks
    ){
        final LineChart<String, Number> chart = new LineChart<>(new CategoryAxis(), createYAxis(maxValue,title,separator,ticks));
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCreateSymbols(false);


        var dtoList = FXCollections.observableArrayList(data.stream().map(e -> new XYChart.Data(e.getXValue(), average)).toList());

        chart.getData().addAll(new XYChart.Series(dtoList));

        return chart;
    }

    private NumberAxis createYAxis(double maxValue,String title,String separatorString,String ticksString){
        double separator;
        try{
            separator = Double.parseDouble(separatorString.replace(",","."));
        }catch(NumberFormatException e){
            separator = 10;
        }

        int ticks;
        try{
            ticks = Integer.parseInt(ticksString);
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

    //endregion

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
        keyActions.put(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN), this::handleSave);

        addEventFilter(chartPane);
    }

    @Override
    public void close() {
        var stage = (Stage) chartPane.getScene().getWindow();
        stage.close();
    }
}
