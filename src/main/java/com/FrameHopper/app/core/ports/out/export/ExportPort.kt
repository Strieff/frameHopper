package com.FrameHopper.app.core.ports.out.export

import com.FrameHopper.app.boundry.dto.export.ExportDataInput

interface ExcelExportPort {
    fun exportToExcelByteArray(input: ExportDataInput): ByteArray
}

interface CSVExportPort {
    fun exportToZipByteArray(input: ExportDataInput): ByteArray

    fun exportVideosToCsvByteArray(input: ExportDataInput): ByteArray
    fun exportVideoSummaryToCsvByteArray(input: ExportDataInput): ByteArray
    fun exportTagsToCsvByteArray(input: ExportDataInput): ByteArray

    fun exportVideosToCSV(input: ExportDataInput): String
    fun exportVideosSummaryToCSV(input: ExportDataInput): String
    fun exportTagsToCSV(input: ExportDataInput): String
}