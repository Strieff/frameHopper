package com.FrameHopper.app.View.FXViews.Charts;

import com.FrameHopper.app.Model.Tag;
import com.FrameHopper.app.Model.Video;
import com.FrameHopper.app.Service.TagService;
import com.FrameHopper.app.Service.VideoService;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXDialogProvider;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ChartsService {
    private final VideoService videoService;
    private final TagService tagService;

    public ChartsService(VideoService videoService, TagService tagService) {
        this.videoService = videoService;
        this.tagService = tagService;
    }

    public ObservableList<TableEntry> getVideos() {
        var videoDTOList = videoService.getAll();
        ObservableList<TableEntry> data = FXCollections.observableArrayList();

        for (var video : videoDTOList) {
            data.add(new TableEntry(
                    video,
                    video.getName()
            ));
        }

        return data;
    }

    //returns complexity of each video
    public Map<Video, Number> getComplexity(List<Video> videos){
        return getTotalPoints(videos).entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> {
                            var df = new DecimalFormat("#.###");
                            var complexity = df.format((double) e.getValue()/e.getKey().getDuration()).replace(",",".");
                            return Double.parseDouble(complexity);
                        }
                ));

    }

    //returns frame count of each video
    public Map<Video, Number> getFrameCount(List<Video> videos) {
        return videos.stream()
                .collect(Collectors.toMap(
                        v -> v,
                        Video::getTotalFrames
                ));
    }

    //returns total points of each video
    public Map<Video, Number> getTotalPoints(List<Video> videos) {
        return videos.stream()
                .collect(Collectors.toMap(
                        v -> v,
                        v -> tagService.getTagsOnVideo(v).stream()
                                .mapToDouble(Tag::getValue)
                                .sum()
                ));

    }

    //returns runtimes of each video
    public Map<Video, Number> getRuntimes(List<Video> videos) {
        return videos.stream()
                .collect(Collectors.toMap(
                        v -> v,
                        Video::getDuration
                ));
    }

    //returns amount of unique tags on each video
    public Map<Video, Number> getUniqueTagsCount(List<Video> videos) {
        var data = tagService.getAmountOfUniqueTagsOnVideos(videos);
        videos.forEach(v -> data.computeIfAbsent(v, n -> 0));

        return data;
    }

    //returns mean
    public <T extends Number> double getMean(Map<String,T> valueMap){
        return valueMap.values().stream()
                .mapToDouble(Number::doubleValue)
                .average()
                .orElse(0D);
    }

    //exports data to csv
    public void export(ChartsActionEntry yAxis, List<Video> videos, String path){
        Map<String,Number> valueMap = yAxis.apply(videos).entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().getName(),
                        Map.Entry::getValue
                ));

        final StringBuilder exportReady = new StringBuilder()
                .append("name;").append(yAxis.getLabel());

        valueMap.forEach((key, value) -> exportReady
                .append("\n")
                .append(key)
                .append(";")
                .append(value.doubleValue()));

        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))){
            writer.write('\uFEFF'); // UTF-8 BOM for Excel
            writer.write(exportReady.toString());
            writer.flush();
        }catch (Exception e){
            FXDialogProvider.errorDialog(e.getMessage());
            e.printStackTrace();
        }
    }

    //load custom data label
    public String getCustomDataLabel(String path) throws Exception{
        var br = new BufferedReader(new FileReader(path));
        return br.readLine().split(";")[1];
    }

    //load data for custom chart
    public List<XYChart.Data<String, Number>> getCustomData(String path) throws Exception{
        List<XYChart.Data<String, Number>> initialData = new ArrayList<>();
        var br = new BufferedReader(new FileReader(path));
        br.lines().skip(1).forEachOrdered(e -> {
            var d = e.split(";");
            initialData.add(new XYChart.Data<>(
                    d[0],
                    Double.parseDouble(d[1])
            ));
        });

        return initialData;
    }

    //get mean from custom data
    public Double getCustomMean(ObservableList<XYChart.Data<String, Number>> data) {
        return data.stream().map(XYChart.Data::getYValue).mapToDouble(Number::doubleValue).average().orElse(100);
    }

    //get layered charts
    public StackPane layerCharts(final XYChart<String,Number>... charts){
        for (XYChart<String, Number> chart : charts)
            configureOverlayChart(chart);

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(charts);

        return stackPane;
    }

    //apply styles to chart
    private void configureOverlayChart(final XYChart<String, Number> chart) {
        chart.setAlternativeRowFillVisible(false);
        chart.setAlternativeColumnFillVisible(false);
        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalGridLinesVisible(false);
        chart.getXAxis().setVisible(false);
        chart.getYAxis().setVisible(false);

        chart.getStylesheets().addAll(getClass().getClassLoader().getResource("styling/overlay-chart.css").toExternalForm());
    }

    //save chart yo file
    public void saveChartAsFile(VBox saveArea,String path){
        WritableImage snapshot = new WritableImage((int) saveArea.getWidth(),(int) saveArea.getHeight());
        saveArea.snapshot(new SnapshotParameters(), snapshot);

        var file = new File(path);
        try{
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot,null),"png",file);
        }catch (Exception e){
            FXDialogProvider.errorDialog(e.getMessage());
            e.printStackTrace();
        }
    }
}