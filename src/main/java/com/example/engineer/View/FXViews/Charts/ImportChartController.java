package com.example.engineer.View.FXViews.Charts;

import com.example.engineer.View.Elements.DataManagers.UserSettingsManager;
import com.example.engineer.View.Elements.FXElementsProviders.FXDialogProvider;
import com.example.engineer.View.Elements.FXElementsProviders.FXIconLoader;
import com.example.engineer.View.Elements.FXElementsProviders.FileChooserProvider;
import com.example.engineer.View.Elements.Language.Dictionary;
import com.example.engineer.View.Elements.Language.LanguageChangeListener;
import com.example.engineer.View.Elements.Language.LanguageManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Comparator;

@Component
@Scope("prototype")
public class ImportChartController implements LanguageChangeListener {
    @FXML
    StackPane chartPane;
    @FXML
    Axis<Number> yAxis,yAxisLine;
    @FXML
    ImageView saveButtonIcon,closeButtonIcon;
    @FXML
    VBox saveArea;
    @FXML
    Rectangle rectangle1,rectangle2;
    @FXML
    Label legend1Label,legend2Label,legend3Label;
    @FXML
    HBox legendBox2,meanArea;

    @Autowired
    ChartsService viewService;
    @Autowired
    UserSettingsManager userSettings;

    @FXML
    public void initialize(){
        LanguageManager.register(this);

        Platform.runLater(() -> {
            var stage = (Stage)saveArea.getScene().getWindow();
            stage.setOnCloseRequest(e -> handleClose());
        });

        saveButtonIcon.setImage(FXIconLoader.getLargeIcon("save.png"));
        closeButtonIcon.setImage(FXIconLoader.getLargeIcon("x.png"));
    }

    public void init(
            String path,
            boolean showMean,
            boolean colorMean,
            String separator,
            String ticks
    ){
        var title = viewService.getCustomDataLabel(path);
        var data = FXCollections.observableArrayList(viewService.getCustomData(path));
        var maxValue = data.stream().map(e->e.getYValue().doubleValue()).max(Comparator.naturalOrder()).orElse(0d);
        chartPane.getChildren().clear();


        if (!showMean)
            chartPane.getChildren().add(createBarChart(data,maxValue,title,colorMean,separator,ticks));
        else
            chartPane.getChildren().add(viewService.layerCharts(createBarChart(
                    data,maxValue,title,colorMean,separator,ticks),
                    createLineChart(data, maxValue,title,separator,ticks)
            ));

        if(!colorMean){
            rectangle1.setFill(Color.valueOf("blue"));
            legendBox2.setVisible(false);
        }else{
            rectangle1.setFill(Color.valueOf("greed"));
            rectangle2.setFill(Color.valueOf("red"));
            legendBox2.setVisible(true);
        }

        meanArea.setVisible(showMean);
    }

    private BarChart<String, Number> createBarChart(
            ObservableList<XYChart.Data<String, Number>> data,
            Double maxValue,
            String title,
            boolean colorMean,
            String separator,
            String ticks
    ) {
        var mean = viewService.getCustomMean(data);

        final BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), createYAxis(maxValue,title,separator,ticks));
        chart.setLegendVisible(false);
        chart.setAnimated(false);

        chart.getData().add(new XYChart.Series(data));
        if(colorMean)
            chart.getData().get(0).getData().forEach(d -> d.getNode().setStyle(
                    d.getYValue().doubleValue()>=mean?
                            "-fx-bar-fill: GREEN":
                            "-fx-bar-fill: RED"
            ));
        else
            chart.getData().get(0).getData().forEach(d -> d.getNode().setStyle("-fx-bar-fill: BLUE"));

        return chart;
    }

    private LineChart<String, Number> createLineChart(
            ObservableList<XYChart.Data<String, Number>> data,
            Double maxValue,
            String title,
            String separator,
            String ticks
    ){
        final LineChart<String, Number> chart = new LineChart<>(new CategoryAxis(), createYAxis(maxValue,title,separator,ticks));
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCreateSymbols(false);


        var mean = viewService.getCustomMean(data);
        var dtoList = FXCollections.observableArrayList(data.stream().map(e -> new XYChart.Data(e.getXValue(), mean)).toList());

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

    @FXML
    private void handleClose(){
        LanguageManager.unregister(this);
        var stage = (Stage) chartPane.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void handleSave(ActionEvent event) {
        var dir = FileChooserProvider.locationFileChooser((Stage)saveArea.getScene().getWindow(), userSettings.getExportPath());
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

    @Override
    public void changeLanguage() {

    }

}
