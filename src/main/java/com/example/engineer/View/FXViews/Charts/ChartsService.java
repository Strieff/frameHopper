package com.example.engineer.View.FXViews.Charts;

import com.example.engineer.Model.Video;
import com.example.engineer.Service.TagService;
import com.example.engineer.Service.VideoService;
import com.example.engineer.View.Elements.DataManagers.TagListManager;
import com.example.engineer.View.Elements.FXElementsProviders.FXDialogProvider;
import com.example.engineer.View.Elements.Language.Dictionary;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ChartsService {
    @Autowired
    VideoService videoService;
    @Autowired
    TagService tagService;
    @Autowired
    private TagListManager tagList;

    private final Map<Video, Map<String,Number>> videoProperties = new HashMap<>();

    public void loadData() {
        videoProperties.clear();
        final Map<Video, Map<String,Long>> videoData = new HashMap<>();

        var videos = videoService.getAll().stream().map(Video::getId).toList();
        if(videos.isEmpty()) return;

        tagService.countTagsOnFramesOfVideo(videos)
                .forEach(o -> videoData
                        .computeIfAbsent((Video)o[0],v -> new HashMap<>())
                        .put((String)o[1],(Long)o[2])
                );

        if(videos.size() != videoData.size())
            getAbsent(
                    videos,
                    videoData.keySet().stream().map(Video::getId).toList()
            ).forEach(e -> videoData.put(e, new HashMap<>()));

        var uniqueTagsOnVideo = tagService.getAmountOfUniqueTagsOnVideos(videoData.keySet().stream().toList());

        for (int i = 0; i < videoData.size(); i++) {
            var map = new HashMap<String,Number>();
            var video = new ArrayList<>(videoData.keySet()).get(i);

            map.put("total frames",video.getTotalFrames());//total frame count
            try{//unique tags on video
                map.put("unique tags",uniqueTagsOnVideo.get(uniqueTagsOnVideo.keySet().stream().filter(v->v.getId() == video.getId()).findFirst().get()));
            }catch (Exception e){
                map.put("unique tags",0);
            }
            var totalPoints = getTotalPoints(videoData.get(video));
            map.put("duration",video.getDuration());//duration
            map.put("total points",totalPoints);//total points
            map.put("complexity",getComplexity(totalPoints,video));//complexity

            videoProperties.put(video,map);
        }
    }

    private List<Video> getAbsent(List<Integer> all,List<Integer> existing){
        var newAll = new ArrayList<>(all);
        newAll.removeAll(existing);

        return videoService.getById(newAll);
    }

    private int getTotalPoints(Map<String,Long> tagData){
        return tagData.entrySet()
                .stream()
                .mapToInt(e -> (int)(tagList.getTag(e.getKey()).getValue() * e.getValue()))
                .sum();
    }

    private double getComplexity(int totalPoints,Video video){
        DecimalFormat df = new DecimalFormat("#.###");
        String complexity = df.format((double)totalPoints/video.getDuration()).replace(",",".");
        return Double.parseDouble(complexity);
    }

    public ObservableList<TableEntry> getVideos() {
        var videoDTOList = videoService.getAll();
        ObservableList<TableEntry> data = FXCollections.observableArrayList();

        for (var video : videoDTOList) {
            data.add(new TableEntry(
                    video.getId(),
                    video.getName()
            ));
        }

        return data;
    }

    //returns complexity of each video
    public Map<String, Number> getComplexity(List<Integer> ids){
        return getData("complexity",ids);
    }

    //returns frame count of each video
    public Map<String, Number> getFrameCount(List<Integer> ids){
        return getData("total frames",ids);
    }

    //returns amount of unique tags on each video
    public Map<String, Number> getUniqueTagsCount(List<Integer> ids){
        return getData("unique tags",ids);
    }

    //returns runtimes of each video
    public Map<String, Number> getRuntimes(List<Integer> ids){
        return getData("duration",ids);
    }

    //returns total points of each video
    public Map<String, Number> getTotalPoints(List<Integer> ids){
        return getData("total points",ids);
    }

    //returns values of each video
    private Map<String, Number> getData(String option, List<Integer> ids) {
        return videoProperties.entrySet().stream()
                .filter(e -> ids.contains(e.getKey().getId()))
                .collect(Collectors.toMap(
                        e -> e.getKey().getName(),
                        e -> e.getValue().get(option)
                ));
    }

    //returns mean
    public <T extends Number> double getMean(Map<String,T> valueMap){
        return valueMap.values().stream()
                .mapToDouble(Number::doubleValue)
                .average()
                .orElse(0D);
    }

    //exports data to csv
    public void export(String yAxis, List<Integer> ids,String path){
        Map<String,Number> valueMap = null;
        if(Dictionary.get("chart.complexity").equals(yAxis)){
            valueMap = getComplexity(ids);
        }else if(Dictionary.get("chart.tags.unique").equals(yAxis)){
            valueMap = getUniqueTagsCount(ids);
        }else if(Dictionary.get("chart.frame-count").equals(yAxis)){
            valueMap = getFrameCount(ids);
        }else if(Dictionary.get("chart.duration").equals(yAxis)){
            valueMap = getRuntimes(ids);
        }else if(Dictionary.get("chart.points").equals(yAxis)){
            valueMap = getTotalPoints(ids);
        }

        final StringBuilder exportReady = new StringBuilder()
                .append("name;").append(yAxis);

        for(var e : valueMap.entrySet())
            exportReady
                    .append("\n")
                    .append(e.getKey())
                    .append(";")
                    .append(e.getValue().doubleValue());

        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))){
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

        chart.getStylesheets().addAll(getClass().getClassLoader().getResource("overlay-chart.css").toExternalForm());
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