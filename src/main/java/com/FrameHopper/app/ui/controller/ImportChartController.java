package com.FrameHopper.app.ui.controller;

import com.FrameHopper.app.View.Elements.FXElementsProviders.FXDialogProvider;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXIconLoader;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FileChooserProvider;
import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.adapters.settings.UserSettingsAdapter;
import com.FrameHopper.app.ui.UiView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Component
@Scope("prototype")
public class ImportChartController implements UiView {
    @FXML
    StackPane chartPane;
    @FXML
    Axis<Number> yAxis, yAxisLine;
    @FXML
    ImageView saveButtonIcon, closeButtonIcon;
    @FXML
    VBox saveArea;
    @FXML
    Rectangle rectangle1, rectangle2;
    @FXML
    Label legend1Label, legend2Label, legend3Label;
    @FXML
    HBox legendBox2, meanArea;

    private final UserSettingsAdapter userSettingsAdapter;

    public ImportChartController(UserSettingsAdapter userSettingsAdapter) {
        this.userSettingsAdapter = userSettingsAdapter;
    }

    @FXML
    public void initialize(){
        saveButtonIcon.setImage(FXIconLoader.getLargeIcon("save.png"));
        closeButtonIcon.setImage(FXIconLoader.getLargeIcon("x.png"));

        Platform.runLater(() -> {
            var stage = (Stage)rectangle1.getScene().getWindow();
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

    @FXML
    public void handleSave() {
        var dir = FileChooserProvider.locationFileChooser(
                (Stage)chartPane.getScene().getWindow(),
                userSettingsAdapter.useRecentExportPath() ? userSettingsAdapter.getRecentExportPath() : ""
        );
        var name = FXDialogProvider.inputDialog();

        if (name == null) return;
        //TODO: ERROR

        var fileDir = dir + File.separator + name + ".png";

        while(new File(fileDir).exists()){
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

        var snapshot = new WritableImage((int) saveArea.getWidth(),(int) saveArea.getHeight());
        saveArea.snapshot(new SnapshotParameters(), snapshot);

        var file = new File(fileDir);

        try{
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot,null),"png",file);
        }catch (Exception e){
            FXDialogProvider.errorDialog(e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleClose() {
        close();
    }


    @Override
    public void close() {
        var stage = (Stage) chartPane.getScene().getWindow();
        stage.close();
    }
}
