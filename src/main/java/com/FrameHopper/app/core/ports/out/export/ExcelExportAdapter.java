package com.FrameHopper.app.core.ports.out.export;

import java.util.Map;

public interface ExcelExportAdapter {
    void exportToExcel(
            Map<String, Map<String, Number>> videoExportData,
            Map<String, Number> videoSummaryExportData,
            Map<String, Map<String, Number>> tagExportData,
            String fileDir
    );

    byte[] exportToExcel(
            Map<String, Map<String, Number>> videoExportData,
            Map<String, Number> videoSummaryExportData,
            Map<String, Map<String, Number>> tagExportData
    );
}
