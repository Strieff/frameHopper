package com.example.engineer.View.FXViews.Export;

import com.example.engineer.Model.Tag;
import com.example.engineer.Model.Video;
import com.example.engineer.Service.TagService;
import com.example.engineer.Service.VideoService;
import com.example.engineer.View.Elements.FXDialogProvider;
import com.example.engineer.View.Elements.Language.Dictionary;
import com.example.engineer.View.Elements.TagListManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ExportService {
    @Autowired
    VideoService videoService;
    @Autowired
    private TagService tagService;
    @Autowired
    private TagListManager tagList;

    public ObservableList<TableEntry> getVideos(){
        var videoDTOList = videoService.getAll();
        ObservableList<TableEntry> data = FXCollections.observableArrayList();

        for(var video : videoDTOList){
            data.add(new TableEntry(
                    video.getId(),
                    video.getName()
            ));
        }

        return data;
    }

    public ObservableList<TableEntry> getVideos(Set<Integer> selectedIds) {
        var data = getVideos();
        data.forEach(e -> {
            if(selectedIds.contains(Integer.valueOf(e.getId())))
                e.setSelected(true);
        });

        return data;
    }

    public ObservableList<TableEntry> getFiltered(ObservableList<TableEntry> items, String text) {
        return items.filtered(item -> item.getName().toLowerCase().contains(text.toLowerCase()));
    }

    public void exportData(String path, Set<Integer> videos, int format) {
        List<Object[]> data = tagService.countTagsOnFramesOfVideo(videos.stream().toList());
        Map<Video, Map<String,Long>> tagAmountOnVideos = new HashMap<>();

        for(var o : data)
            tagAmountOnVideos
                    .computeIfAbsent((Video)o[0],v -> new HashMap<>())
                    .put((String)o[1],(Long)o[2]);

        if(format == 0) exportExcel(path,tagAmountOnVideos);
        else exportCSV(path,tagAmountOnVideos);
    }

    private void exportExcel(String path,Map<Video, Map<String,Long>> videoTagMap){
        var uniqueTagsOnVideos = getAmountOfUniqueVideos(videoTagMap.keySet().stream().toList());

        String filePath = path+".xlsx";

        try(
                Workbook workbook = WorkbookFactory.create(true);
                FileOutputStream outputStream = new FileOutputStream(filePath)
        ){
            //create general data
            Sheet sheet = workbook.createSheet(Dictionary.get("data.overview.title"));

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue(Dictionary.get("data.overview.name"));
            headerRow.createCell(1).setCellValue(Dictionary.get("data.overview.frameCount"));
            headerRow.createCell(2).setCellValue(Dictionary.get("data.overview.codes"));
            headerRow.createCell(3).setCellValue(Dictionary.get("data.overview.runtime"));
            headerRow.createCell(4).setCellValue(Dictionary.get("data.overview.framerate"));
            headerRow.createCell(5).setCellValue(Dictionary.get("data.overview.totalPoints"));
            headerRow.createCell(6).setCellValue(Dictionary.get("data.overview.complexity"));

            //create data row
            for (int row = 0; row < videoTagMap.size(); row++) {
                Video video = new ArrayList<>(videoTagMap.keySet()).get(row);

                Row dataRow = sheet.createRow(row+1);

                dataRow.createCell(0).setCellValue(video.getName());
                dataRow.createCell(1).setCellValue(video.getTotalFrames());
                dataRow.createCell(2).setCellValue(uniqueTagsOnVideos.get(uniqueTagsOnVideos.keySet().stream().filter(v -> v.getId() == video.getId()).findFirst().get()));
                dataRow.createCell(3).setCellValue(video.getDuration());
                dataRow.createCell(4).setCellValue(video.getFrameRate());
                dataRow.createCell(5).setCellValue(getTotalPoints(videoTagMap.get(video)));
                dataRow.createCell(6).setCellValue(getComplexity(getTotalPoints(videoTagMap.get(video)),video));
            }

            Row summaryRow = sheet.createRow(videoTagMap.size()+2);
            summaryRow.createCell(0).setCellValue(Dictionary.get("data.overview.summary.totalShotAmount"));
            summaryRow.createCell(1).setCellValue(Dictionary.get("data.overview.summary.totalFrameCount"));
            summaryRow.createCell(3).setCellValue(Dictionary.get("data.overview.summary.totalRuntime"));
            summaryRow.createCell(5).setCellValue(Dictionary.get("data.overview.summary.totalPoints"));
            summaryRow.createCell(6).setCellValue(Dictionary.get("data.overview.summary.overallComplexity"));
            summaryRow.createCell(7).setCellValue(Dictionary.get("data.overview.asl"));

            CellStyle decimalStyle = workbook.createCellStyle();
            DataFormat df = workbook.createDataFormat();
            decimalStyle.setDataFormat(df.getFormat("0.000"));

            Row summaryDataRow = sheet.createRow(videoTagMap.size()+3);
            summaryDataRow.createCell(0).setCellValue(videoTagMap.size());
            summaryDataRow.createCell(1).setCellFormula("SUM(B2:B"+ (videoTagMap.size()+1) +")");
            summaryDataRow.createCell(3).setCellFormula("SUM(D2:D"+ (videoTagMap.size()+1) +")");
            summaryDataRow.createCell(5).setCellFormula("SUM(F2:F"+ (videoTagMap.size()+1) +")");
            summaryDataRow.createCell(6).setCellFormula("F" + (videoTagMap.size()+4) + "/D" + (videoTagMap.size()+4));
            sheet.getRow(videoTagMap.size()+3).getCell(6).setCellStyle(decimalStyle);
            summaryDataRow.createCell(7).setCellFormula("D" + (videoTagMap.size()+4) + "/A" + (videoTagMap.size()+4));

            //resize columns
            for (int j = 0; j < 7; j++)
                sheet.autoSizeColumn(j);

            //create tag data info
            Sheet tagSheet = workbook.createSheet(Dictionary.get("data.tags.title"));

            Row tagHeaderRow = tagSheet.createRow(0);
            tagHeaderRow.createCell(0).setCellValue(Dictionary.get("data.tags.name"));
            tagHeaderRow.createCell(1).setCellValue(Dictionary.get("data.tags.value"));
            tagHeaderRow.createCell(2).setCellValue(Dictionary.get("data.tags.amount"));
            tagHeaderRow.createCell(3).setCellValue(Dictionary.get("data.tags.totalPoints"));

            //get all amount of tags on all videos
            Map<String,Long> tagData = getTotalAmountOfTags(videoTagMap);

            int i = 1;
            for(String s : tagData.keySet()){
                Tag tag = tagList.getTag(s);

                Row infoRow = tagSheet.createRow(i++);
                infoRow.createCell(0).setCellValue(s);
                infoRow.createCell(1).setCellValue(tag.getValue());
                infoRow.createCell(2).setCellValue(tagData.get(s));
                infoRow.createCell(3).setCellValue(tag.getValue()*tagData.get(s));
            }

            //resize columns
            for (int j = 0; j < 4; j++)
                tagSheet.autoSizeColumn(j);

            // Write the workbook to a file
            workbook.write(outputStream);
            outputStream.flush();

        }catch (Exception e) {
            FXDialogProvider.errorDialog(e.getMessage());
        }
    }

    private void exportCSV(String path,Map<Video, Map<String,Long>> videoTagMap){
        var uniqueTagsOnVideos = getAmountOfUniqueVideos(videoTagMap.keySet().stream().toList());

        new File(path).mkdirs();

        //save overview
        String overviewFileName = File.separator + Dictionary.get("data.overview.title")+".csv";

        List<String[]> overviewData = new ArrayList<>();
        overviewData.add(new String[]{
                Dictionary.get("data.overview.name"),
                Dictionary.get("data.overview.frameCount"),
                Dictionary.get("data.overview.codes"),
                Dictionary.get("data.overview.runtime"),
                Dictionary.get("data.overview.framerate"),
                Dictionary.get("data.overview.totalPoints"),
                Dictionary.get("data.overview.complexity")
        });

        for (int row = 0; row < videoTagMap.size();row++) {
            Video video = new ArrayList<>(videoTagMap.keySet()).get(row);

            overviewData.add(new String[]{
                    video.getName(),
                    String.valueOf(video.getTotalFrames()),
                    String.valueOf(uniqueTagsOnVideos.get(uniqueTagsOnVideos.keySet().stream().filter(v -> v.getId() == video.getId()).findFirst().get())),
                    String.valueOf(video.getDuration()),
                    String.valueOf(video.getFrameRate()),
                    String.valueOf(getTotalPoints(videoTagMap.get(video))),
                    String.valueOf(getComplexity(getTotalPoints(videoTagMap.get(video)),video))
            });
        }
        writeToCSV(overviewData,path+overviewFileName);

        //save tag data
        String detailsFileName = File.separator + Dictionary.get("data.tags.title") + ".csv";

        Map<String,Long> tagData = getTotalAmountOfTags(videoTagMap);

        List<String[]> tagDetailsData = new ArrayList<>();
        tagDetailsData.add(new String[]{
                Dictionary.get("data.tags.name"),
                Dictionary.get("data.tags.value"),
                Dictionary.get("data.tags.amount"),
                Dictionary.get("data.tags.totalPoints")
        });

        for(String s : tagData.keySet()) {
            Tag tag = tagList.getTag(s);

            tagDetailsData.add(new String[]{
                    s,
                    String.valueOf(tag.getValue()),
                    String.valueOf(tagData.get(s)),
                    String.valueOf(tag.getValue() * tagData.get(s))
            });
        }

        writeToCSV(tagDetailsData,path+detailsFileName);
    }

    //writes data to CSV
    private void writeToCSV(List<String[]> data,String path){
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))) {
            writer.write('\uFEFF');

            String csvText = data.stream()
                    .map(record -> String.join(";", record))
                    .collect(Collectors.joining("\n"));

            writer.write(csvText);
            writer.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private Map<Video,Long> getAmountOfUniqueVideos(List<Video> videos){
        return tagService.getAmountOfUniqueTagsOnVideos(videos);
    }

    //gets total amount of points of given tag
    private int getTotalPoints(Map<String,Long> tagData){
        return tagData.entrySet()
                .stream()
                .mapToInt(entry -> (int)(tagList.getTag(entry.getKey()).getValue() * entry.getValue()))
                .sum();
    }

    //gets complexity of given video
    private double getComplexity(int totalPoints,Video video){
        DecimalFormat df = new DecimalFormat("#.###");
        String complexity = df.format((double)totalPoints/video.getDuration()).replace(',','.');
        return Double.parseDouble(complexity);
    }

    //gets total amount of tags
    private Map<String,Long> getTotalAmountOfTags(Map<Video,Map<String,Long>> videoTagMap){
        Map<String,Long> tagData = new HashMap<>();

        videoTagMap.values()
                .stream()
                .flatMap(data -> data.entrySet().stream())
                .forEach(entry -> tagData.merge(entry.getKey(), entry.getValue(), Long::sum));

        return tagData;
    }
}
