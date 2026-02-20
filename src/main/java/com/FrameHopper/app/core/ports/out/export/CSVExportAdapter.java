package com.FrameHopper.app.core.ports.out.export;

import java.util.Map;

public interface CSVExportAdapter {
    void exportToCSV(
            Map<String, Map<String, Number>> videoExportData,
            Map<String, Number> videoSummaryExportData,
            Map<String, Map<String, Number>> tagExportData,
            String dir
    );
}
