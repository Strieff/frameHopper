package com.FrameHopper.app.View.FXViews.Export;

import com.FrameHopper.app.Model.Tag;
import com.FrameHopper.app.Model.Video;
import com.FrameHopper.app.Service.TagService;
import com.FrameHopper.app.Service.VideoService;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXDialogProvider;
import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.View.Elements.DataManagers.TagListManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ExportService {
    private final VideoService videoService;
    private final TagService tagService;
    private final TagListManager tagList;

    public ExportService(VideoService videoService, TagService tagService, TagListManager tagList) {
        this.videoService = videoService;
        this.tagService = tagService;
        this.tagList = tagList;
    }

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
            if(selectedIds.contains(e.getId()))
                e.setSelected(true);
        });

        return data;
    }

    public ObservableList<TableEntry> getFiltered(ObservableList<TableEntry> items, String text) {
        return items.filtered(item -> item.getName().toLowerCase().contains(text.toLowerCase()));
    }

    public void exportData(String path,
                           List<Integer> videoIds,
                           LinkedList<ExportActionEntry.VideoExportActionEntry> videoEntries,
                           LinkedList<ExportActionEntry.TagExportActionEntry> tagEntries,
                           int format,
                           String languageCode
    ) {
        //VIDEO DATA
        var videos = videoService.getAll(videoIds);

        var videoDataNames = videoEntries.stream()
                .filter(ExportActionEntry::isSelected)
                .map(e -> Dictionary.get(languageCode, e.getLabelName()))
                .toList();

        var mappedVideoData = videos.stream()
                .collect(Collectors.toMap(
                        Video::getName,
                        video -> new Pair<>(
                                video.getName(),
                                videoEntries.stream()
                                    .filter(ExportActionEntry::isSelected)
                                    .map(i -> i.apply(video))
                                    .toList()
                        )
                ));

        var summaryData = buildSummaryData(videoEntries, videos).sequencedEntrySet().stream()
                .filter(d -> {
                    var fields = new ArrayList<>(videoEntries.stream()
                            .filter(ExportActionEntry::isSelected)
                            .map(e -> e.getLabelName().split("\\.")[2])
                            .toList());
                    fields.add("asl");

                    var namespace = d.getKey().split("\\.");
                    if (namespace.length == 1) return true;

                    return fields.contains(namespace.length < 4 ? namespace[2] : namespace[3]);
                }).collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a,b) -> a,
                        LinkedHashMap::new
                ));

        //TAG DATA
        var tags = tagService.getTagsOnVideos(videos);

        var tagDataNames = tagEntries.stream()
                .filter(ExportActionEntry::isSelected)
                .map(e -> Dictionary.get(languageCode, e.getLabelName()))
                .toList();

        var mappedTagData = tags.stream()
                .collect(Collectors.toMap(
                        Tag::getName,
                        tag -> new Pair<>(
                                tag.getName(),
                                tagEntries.stream()
                                        .filter(ExportActionEntry::isSelected)
                                        .map(i -> i.apply(tag, videos))
                                        .toList()
                        )
                ));

        //HANDLE EXPORT
        if(format == 0) exportToExcel(
                path,
                videoDataNames,
                mappedVideoData,
                tagDataNames,
                mappedTagData,
                summaryData,
                languageCode
        );
        else exportToCSV(
                path,
                videoDataNames,
                mappedVideoData,
                tagDataNames,
                mappedTagData,
                languageCode
        );

    }

    private static LinkedHashMap<String, Double> buildSummaryData(
            List<ExportActionEntry.VideoExportActionEntry> videoEntries,
            List<Video> videos
    ) {
        Function<String, Double> sumForField = field ->
                videos.stream()
                        .map(v -> videoEntries.stream()
                                .filter(e -> e.getLabelName() != null && e.getLabelName().contains(field))
                                .map(e -> e.apply(v))
                                .filter(java.util.Objects::nonNull)
                                .mapToDouble(Number::doubleValue)
                                .sum()
                        )
                        .mapToDouble(Double::doubleValue)
                        .sum();

        // precompute
        double frameCount  = sumForField.apply("frameCount");
        double runtime     = sumForField.apply("runtime");
        double totalPoints = sumForField.apply("totalPoints");
        double complexity  = (runtime == 0d) ? Double.NaN : (totalPoints / runtime);
        double asl = totalPoints/ videoEntries.size();

        Map<String, Double> summaryByField = new HashMap<>();
        summaryByField.put("frameCount", frameCount);
        summaryByField.put("runtime", runtime);
        summaryByField.put("totalPoints", totalPoints);
        summaryByField.put("complexity", complexity);

        // build final map
        var finalMap = videoEntries.stream()
                .collect(Collectors.toMap(
                        i -> {
                            String s = i.getSummaryLabelName();
                            return (s == null || s.isBlank()) ? i.getLabelName() : s;
                        },
                        i -> {
                            String resolved = (i.getSummaryLabelName() == null || i.getSummaryLabelName().isBlank())
                                    ? i.getLabelName()
                                    : i.getSummaryLabelName();

                            if (resolved == null) return Double.NaN;

                            String[] parts = resolved.split("\\.");
                            if (parts.length < 4) return Double.NaN;
                            String field = parts[parts.length - 1];

                            return summaryByField.getOrDefault(field, Double.NaN);
                        },
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        finalMap.put("asl",asl);

        return finalMap;
    }


    private void exportToExcel(
            String path,
            List<String> videoHeaders,
            Map<String, Pair<String, List<Number>>> mappedVideoData,
            List<String> tagHeaders,
            Map<String, Pair<String, List<Number>>> mappedTagData,
            LinkedHashMap<String, Double> summaryData,
            String language
    ) {
        String filePath = path + ".xlsx";

        try (
                Workbook workbook = WorkbookFactory.create(true);
                FileOutputStream fos = new FileOutputStream(filePath)
        ){
            //create video data sheet
            var videoDataSheet = setUpExcelSheet(
                    workbook,
                    "overview",
                    videoHeaders,
                    mappedVideoData,
                    language
            );

            setUpSummaryData(videoDataSheet, summaryData, mappedVideoData.size(), language);

            //create tag data sheet
            setUpExcelSheet(
                    workbook,
                    "tags",
                    tagHeaders,
                    mappedTagData,
                    language
            );

            //style
            CellStyle decimalStyle = workbook.createCellStyle();
            DataFormat df = workbook.createDataFormat();
            decimalStyle.setDataFormat(df.getFormat("0.000"));

            //write to file
            workbook.write(fos);
            fos.flush();
        } catch (Exception e) {
            FXDialogProvider.errorDialog(e.getMessage());
            e.printStackTrace();
        }
    }

    private Sheet setUpExcelSheet(
            Workbook workbook,
            String namespace,
            List<String> headers,
            Map<String, Pair<String, List<Number>>> mappedData,
            String language
    ){
        //create data sheet
        Sheet sheet = workbook.createSheet(Dictionary.get(language, String.format("data.%s.title",namespace)));

        //create headers row
        var tagHeaderRow = sheet.createRow(0);
        tagHeaderRow.createCell(0).setCellValue(Dictionary.get(language, String.format("data.%s.name",namespace)));
        for (int i = 0; i < headers.size(); i++)
            tagHeaderRow.createCell(i + 1).setCellValue(headers.get(i));

        //create data rows
        for (int row = 0; row < mappedData.size(); row++) {
            Row dataRow = sheet.createRow(row + 1);
            var key = mappedData.keySet().stream().toList().get(row);
            var value = mappedData.get(key);

            //put name
            dataRow.createCell(0).setCellValue(value.getKey());

            //put data
            for (int column = 0; column < value.getValue().size(); column++)
                dataRow.createCell(column + 1).setCellValue(value.getValue().get(column).doubleValue());
        }

        //resize cells
        for (int i = 0; i < headers.size() + 1; i++)
            sheet.autoSizeColumn(i);

        return sheet;
    }

    private void setUpSummaryData(Sheet sheet, LinkedHashMap<String, Double> data, int entries, String language) {
        var summaryHeaderRow = sheet.createRow(entries + 2);
        summaryHeaderRow.createCell(0).setCellValue(Dictionary.get(language, "data.overview.summary.totalShotAmount"));
        sheet.autoSizeColumn(0);
        for (int column = 0; column < data.size(); column++) {
            String dictionaryForm = data.keySet().stream().toList().get(column);
            summaryHeaderRow.createCell(column + 1).setCellValue(dictionaryForm.split("\\.").length != 4 ? "" : Dictionary.get(language, dictionaryForm));
            sheet.autoSizeColumn(column + 1);
        }
        summaryHeaderRow.createCell(data.size()).setCellValue(Dictionary.get(language, "data.overview.asl"));

        var summaryDataRow = sheet.createRow(entries + 3);
        summaryDataRow.createCell(0).setCellValue(entries);
        for (int column = 0; column < data.size(); column++) {
            var key = new ArrayList<>(data.keySet()).get(column);
            var value = data.get(key);
            if(!Double.isNaN(value))
                summaryDataRow.createCell(column + 1).setCellValue(value);
            sheet.autoSizeColumn(column + 1);
        }

    }

    private void exportToCSV(
            String path,
            List<String> videoHeaders,
            Map<String, Pair<String, List<Number>>> mappedVideoData,
            List<String> tagHeaders,
            Map<String, Pair<String, List<Number>>> mappedTagData,
            String language
    ) {
        new File(path).mkdirs();

        //video data
        var videoDataFilePath = getCSVFilePath(path, "overview", language);
        var videoData = getCsvData(
                "overview",
                videoHeaders,
                mappedVideoData,
                language
        );

        //write video data file
        writeToCSV(videoData, videoDataFilePath);

        //tag data
        var tagDataFilePath  = getCSVFilePath(path, "tags", language);
        var tagData = getCsvData(
                "tags",
                tagHeaders,
                mappedTagData,
                language
        );

        //write tag data file
        writeToCSV(tagData, tagDataFilePath);
    }

    private String getCSVFilePath(String path, String namespace, String language) {
        return path + File.separator + Dictionary.get(language, String.format("data.%s.title",namespace))+".csv";
    }

    private List<List<String>> getCsvData(
            String namespace,
            List<String> headers,
            Map<String, Pair<String, List<Number>>> mappedData,
            String language
    ) {
        //write data
        List<List<String>> data = new ArrayList<>();

        //add video headers row
        List<String> headerList = new ArrayList<>();
        headerList.add(Dictionary.get(language, String.format("data.%s.name",namespace)));
        headerList.addAll(headers);
        data.add(headerList);

        //add video data rows
        for(var row : mappedData.values()) {
            List<String> rowData = new ArrayList<>();
            rowData.add(row.getKey());
            row.getValue().forEach(v -> rowData.add(v.toString()));
            data.add(rowData);
        }

        return data;
    }

    //write data to csv file
    private void writeToCSV(List<List<String>> data, String path) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))) {
            writer.write('\uFEFF'); // UTF-8 BOM for Excel
            String csvText = data.stream()
                    .map(row -> String.join(";", row))
                    .collect(Collectors.joining("\n"));

            writer.write(csvText);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public int getTotalTagsOnVideo(Video video) {
        return tagService.countTagsOnFramesOfVideo(Collections.singletonList(video.getId())).size();
    }

    public int getTotalPoints(Video video){
        return tagService.countTagsOnFramesOfVideo(Collections.singletonList(video.getId())).stream()
                .mapToInt(o -> {
                    String tagName = (String) o[1];
                    long count = (Long) o[2];
                    return (int) (tagList.getTag(tagName).getValue() * count);
                })
                .sum();
    }

    public int getAmount(Tag tag, List<Video> videos) {
        return tagService.getTotalTagAmountOnVideos(tag,videos).size();
    }

    public double getTotalPoints(Tag tag, List<Video> videos) {
        return tagService.getTotalTagAmountOnVideos(tag, videos).stream()
                .mapToDouble(TagOnVideoDto::value)
                .sum();
    }

    public double getComplexity(Video video) {
        var points = getTotalPoints(video);
        var df = new DecimalFormat("#.###");
        var complexity = df.format(points/video.getDuration()).replace(",",".");
        return Double.parseDouble(complexity);
    }
}
