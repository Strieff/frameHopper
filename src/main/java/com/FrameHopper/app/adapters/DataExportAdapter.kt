package com.FrameHopper.app.adapters;

import com.FrameHopper.app.core.ports.out.export.CSVExportAdapter;
import com.FrameHopper.app.core.ports.out.export.ExcelExportAdapter;
import com.FrameHopper.app.ui.language.I18n;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class DataExportAdapter implements ExcelExportAdapter, CSVExportAdapter {
    @Override
    public void exportToCSV(
            Map<String, Map<String, Number>> videoExportData,
            Map<String, Number> videoSummaryExportData,
            Map<String, Map<String, Number>> tagExportData,
            String dir
    ) {
        var videoData = getVideosData(videoExportData);

        var summaryData = getVideosSummaryData(videoSummaryExportData);
        var dummySummaryPath = dir + File.separator + "summary.csv";

        var tagData = getTagsData(tagExportData);

        writeToFile(videoData, getCSVFilePath(dir,"video"));
        writeToFile(summaryData, dummySummaryPath);
        writeToFile(tagData, getCSVFilePath(dir,"tags"));
    }

    @Override
    public String getVideosData(Map<String, Map<String, Number>> videoExportData) {
        return getCSVData(videoExportData, "video");
    }

    @Override
    public String getVideosSummaryData(Map<String, Number> videoSummaryExportData) {
        List<List<String>> data = new LinkedList<>();
        data.add(videoSummaryExportData.keySet().stream().filter(e -> !e.contains("undefined")).toList());
        data.add(videoSummaryExportData.values().stream().filter(Objects::nonNull).map(Number::toString).toList());

        return data.stream()
                .map(row -> String.join(";", row))
                .collect(Collectors.joining("\n"));
    }

    @Override
    public String getTagsData(Map<String, Map<String, Number>> tagExportData) {
        return getCSVData(tagExportData, "tags");
    }

    //region [CSV Helpers]

    private String getCSVData(Map<String, Map<String, Number>> data, String namespace) {
        var headers = new LinkedList<>(data.values().stream().findFirst().get().keySet());
        var rows = new LinkedList<List<String>>();

        var headerRow = new LinkedList<String>();
        headerRow.add(I18n.tr(String.format("export.data.%s.name", namespace)));
        headerRow.addAll(headers);
        rows.add(headerRow);

        for(var entry : data.entrySet()) {
            var dataRow = new LinkedList<String>();
            dataRow.add(entry.getKey());

            for(var dataPoint : entry.getValue().entrySet())
                dataRow.add(dataPoint.getValue().toString());

            rows.add(dataRow);
        }

        return rows.stream()
                .map(row -> String.join(";",row))
                .collect(Collectors.joining("\n"));
    }

    private String getCSVFilePath(String path, String namespace) {
        return path + File.separator + I18n.tr(String.format("export.data.%s.title", namespace)) + ".csv";
    }

    private void writeToFile(String data, String path) {
        var outFile = new File(path);

        if(outFile.getParentFile() != null && !outFile.getParentFile().exists())
            outFile.getParentFile().mkdirs();

        try (var writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))) {
            writer.write('\uFEFF'); // UTF-8 BOM
            writer.write(data);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //endregion

    @Override
    public void exportToExcel(
            Map<String, Map<String, Number>> videoExportData,
            Map<String, Number> videoSummaryExportData,
            Map<String, Map<String, Number>> tagExportData,
            String fileDir
    ) {
        try(
                Workbook workbook = createWorkbook(videoExportData, videoSummaryExportData, tagExportData);
                var fos = new FileOutputStream(fileDir)
        ) {
            workbook.write(fos);
            fos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] exportToExcel(
            Map<String, Map<String, Number>> videoExportData,
            Map<String, Number> videoSummaryExportData,
            Map<String, Map<String, Number>> tagExportData
    ) {
        try (
                Workbook workbook = createWorkbook(videoExportData, videoSummaryExportData, tagExportData);
                ByteArrayOutputStream bos = new ByteArrayOutputStream()
        ) {
            workbook.write(bos);
            workbook.close();

            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //region [Excel Helpers]

    private Workbook createWorkbook(
            Map<String, Map<String, Number>> videoExportData,
            Map<String, Number> videoSummaryExportData,
            Map<String, Map<String, Number>> tagExportData) {
        try {
            Workbook workbook = WorkbookFactory.create(true);

            var videoDataSheet = setUpExcelSheet(workbook, "video", videoExportData);
            addSummaryData(videoDataSheet, videoSummaryExportData);
            setUpExcelSheet(workbook, "tags", tagExportData);

            CellStyle decimalStyle = workbook.createCellStyle();
            DataFormat df = workbook.createDataFormat();
            decimalStyle.setDataFormat(df.getFormat("0.000"));

            return workbook;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addSummaryData(Sheet sheet, Map<String, Number> summaryData) {
        var lastColumn =  sheet.getLastRowNum();
        var summaryHeaderRow = sheet.createRow( lastColumn+ 2);
        var summaryDataRow = sheet.createRow(lastColumn + 3);

        int column = 0;
        for(var entry : summaryData.entrySet()) {
            if(entry.getKey().contains("undefined")) {
                column++;
                continue;
            }

            summaryHeaderRow.createCell(column).setCellValue(entry.getKey());
            summaryDataRow.createCell(column++).setCellValue(entry.getValue().doubleValue());
        }

        for (int i = 0; i < summaryData.size(); i++)
            sheet.autoSizeColumn(i);
    }

    private Sheet setUpExcelSheet(Workbook workbook, String namespace, Map<String, Map<String, Number>> data) {
        var headers = new LinkedList<>(data.values().stream().findFirst().get().keySet());

        Sheet sheet = workbook.createSheet(I18n.tr(String.format("export.data.%s.title",namespace)));

        var headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue(I18n.tr(String.format("export.data.%s.name",namespace)));
        for (int i = 0; i < headers.size() ; i++)
            headerRow.createCell(i + 1).setCellValue(headers.get(i));

        int row = 1;
        for (var entry : data.entrySet()) {
            var dataRow = sheet.createRow(row++);
            dataRow.createCell(0).setCellValue(entry.getKey());

            int column = 1;
            for (var dataEntry : new LinkedList<>(entry.getValue().values()))
                dataRow.createCell(column++).setCellValue(dataEntry.doubleValue());
        }

        for (int i = 0; i < headers.size() + 1; i++)
            sheet.autoSizeColumn(i);

        return sheet;
    }

    //endregion
}
