package com.FrameHopper.app.adapters

import com.FrameHopper.app.boundry.dto.VideoDTO
import com.FrameHopper.app.boundry.dto.export.ExportDataInput
import com.FrameHopper.app.boundry.dto.analytics.VideoDataDTO
import com.FrameHopper.app.boundry.dto.export.ChosenTagAnalytics
import com.FrameHopper.app.boundry.dto.export.ChosenVideoAnalytics
import com.FrameHopper.app.core.application.analytics.TagAnalyticsQuery
import com.FrameHopper.app.core.application.analytics.VideoAnalyticsQuery
import com.FrameHopper.app.core.ports.`in`.frame.FrameQuery
import com.FrameHopper.app.core.ports.`in`.tag.TagsQuery
import com.FrameHopper.app.core.ports.out.export.CSVExportPort
import com.FrameHopper.app.core.ports.out.export.ExcelExportPort
import com.FrameHopper.app.ui.language.I18n
import com.fasterxml.jackson.annotation.JsonCreator
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Component
class DataExportAdapter(
    val frameQuery: FrameQuery,
    val tagsQuery: TagsQuery,
    val videoAnalyticsQuery: VideoAnalyticsQuery,
    val tagAnalyticsQuery: TagAnalyticsQuery
) : ExcelExportPort, CSVExportPort {
    companion object {
        const val VIDEO_COUNT: String = "export.data.video.name"
        const val VIDEO_COUNT_SUMMARY = "export.data.video.count"
        const val VIDEO_FILE_NAME: String = "export.data.video.title"
        const val VIDEO_SUMMARY_FILE_NAME: String = "export.data.video.summary.title"
        const val TAG_COUNT: String = "export.data.tags.name"
        const val TAG_FILE_NAME: String = "export.data.tags.title"
        const val ASL = "export.data.video.summary.asl"
        const val UTF8_BOM = "\uFEFF"
    }

    override fun exportToExcelByteArray(input: ExportDataInput): ByteArray {
        val videoMap = getVideoExportData(input)
        val tagMap = getTagExportData(input)

        val videoData = exportVideosToCSV(videoMap, input.chosenVideoAnalytics).split("\n").map { it.split(";") }
        val videoSummaryData = exportVideosSummaryToCSV(videoMap, input.chosenVideoAnalytics).split("\n").map { it.split(";") }
        val tagData = exportTagsToCSV(tagMap, input.chosenTagAnalytics).split("\n").map { it.split(";") }

        val workbook = WorkbookFactory.create(true)
        val format = workbook.createDataFormat()
        val doubleStyle = workbook.createCellStyle().apply {
            dataFormat = format.getFormat("#,##0.000")
        }
        val intStyle = workbook.createCellStyle().apply {
            dataFormat = format.getFormat("#,##0")
        }

        val videoSheet = workbook.createSheet(I18n.tr(VIDEO_FILE_NAME))

        addDataToSheet(
            sheet = videoSheet,
            doubleStyle = doubleStyle,
            intStyle = intStyle,
            data = videoData
        )

        addDataToSheet(
            sheet = videoSheet,
            doubleStyle = doubleStyle,
            intStyle = intStyle,
            startIndex = videoData.size + 2,
            data = videoSummaryData
        )

        resizeCells(videoSheet, videoSummaryData[0].size - 1)

        val tagSheet = workbook.createSheet(I18n.tr(TAG_FILE_NAME))

        addDataToSheet(
            sheet = tagSheet,
            doubleStyle = doubleStyle,
            intStyle = intStyle,
            data = tagData
        )

        resizeCells(tagSheet, tagData[0].size - 1)

        return ByteArrayOutputStream().use { baos ->
            workbook.use {
                it.write(baos)
            }
            baos.toByteArray()
        }
    }

    //region [Excel utilities]

    private fun addDataToSheet(sheet: Sheet, doubleStyle: CellStyle, intStyle: CellStyle, startIndex: Int = 0, data: List<List<String>>) =
        data.forEachIndexed { rowIndex, rowData ->
            val row = sheet.createRow(rowIndex + startIndex)
            rowData.forEachIndexed { columnIndex, columnData ->
                val cell = row.createCell(columnIndex)
                val numberData = columnData.toDoubleOrNull()
                if (numberData != null) {
                    cell.cellStyle = if (numberData % 1 == 0.0) intStyle else doubleStyle
                    cell.setCellValue(numberData)
                } else {
                    val tryKey = I18n.tr(columnData)
                    cell.setCellValue(if ("!${columnData}!" == tryKey) columnData else tryKey)
                }
            }
        }


    private fun resizeCells(sheet: Sheet, to: Int) = (0 until to).forEach { i ->
            sheet.autoSizeColumn(i)
        }

    //endregion

    override fun exportToZipByteArray(input: ExportDataInput): ByteArray {
        val videoMap = getVideoExportData(input)
        val tagMap = getTagExportData(input)

        val videoDataString = exportVideosToCSV(videoMap, input.chosenVideoAnalytics)
        val videoSummaryString = exportVideosSummaryToCSV(videoMap, input.chosenVideoAnalytics)
        val tagDataString = exportTagsToCSV(tagMap, input.chosenTagAnalytics)

        val files = listOf(
            "${I18n.tr(VIDEO_FILE_NAME)}.csv" to videoDataString ,
            "${I18n.tr(VIDEO_SUMMARY_FILE_NAME)}.csv" to videoSummaryString,
            "${I18n.tr(TAG_FILE_NAME)}.csv" to tagDataString
        )

        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos, Charsets.UTF_8).use { zos ->
            for ((fileName, content) in files) {
                zos.putNextEntry(ZipEntry(fileName))
                zos.write(UTF8_BOM.toByteArray(Charsets.UTF_8))
                zos.write(content.toByteArray(Charsets.UTF_8))
                zos.closeEntry()
            }
        }

        return baos.toByteArray()
    }

    //region [Get CSV files]

    override fun exportVideosToCsvByteArray(input: ExportDataInput): ByteArray = (UTF8_BOM + exportVideosToCSV(input)).toByteArray(Charsets.UTF_8)

    override fun exportVideoSummaryToCsvByteArray(input: ExportDataInput): ByteArray = (UTF8_BOM + exportVideosSummaryToCSV(input)).toByteArray(Charsets.UTF_8)

    override fun exportTagsToCsvByteArray(input: ExportDataInput): ByteArray = (UTF8_BOM + exportTagsToCSV(input)).toByteArray(Charsets.UTF_8)

    //endregion

    //region [Get CSV format string]
    override fun exportVideosToCSV(input: ExportDataInput): String =
        exportVideosToCSV(getVideoExportData(input), input.chosenVideoAnalytics)

    private fun exportVideosToCSV(input: VideoMap, analytics: List<ChosenVideoAnalytics>): String = listOf(
            listOf(
                I18n.tr(VIDEO_COUNT),
                analytics.map { I18n.tr(it.label) }.toList().joinToString(";")
            ).joinToString(separator = ";"),
            input.getNames().joinToString("\n") {
                listOf(
                    it,
                    analytics.joinToString(";") { a -> a.extractor(input, it).toString() }
                ).joinToString(";")
            }
        ).joinToString("\n")

    override fun exportVideosSummaryToCSV(input: ExportDataInput): String =
        exportVideosSummaryToCSV(getVideoExportData(input), input.chosenVideoAnalytics)

    private fun exportVideosSummaryToCSV(input: VideoMap, analytics: List<ChosenVideoAnalytics>): String = mutableListOf(
            listOf(
                I18n.tr(VIDEO_COUNT_SUMMARY),
                analytics.joinToString(";") { if (it.summaryLabel.isNullOrEmpty()) " " else I18n.tr(it.summaryLabel) },
                I18n.tr(ASL)
            ).joinToString(";"),
            listOf(
                input.getNames().size,
                analytics.map { it.summaryExtractor?.let { e -> e(input) } ?: " " }.joinToString(";"),
                input.asl
            ).joinToString(";")
        ).joinToString(separator = "\n")

    override fun exportTagsToCSV(input: ExportDataInput): String =
        exportTagsToCSV(getTagExportData(input), input.chosenTagAnalytics)

    private fun exportTagsToCSV(input: TagMap, analytics: List<ChosenTagAnalytics>): String = listOf(
        listOf(
            I18n.tr(TAG_COUNT),
            analytics.joinToString(";") { I18n.tr(it.label) }
        ).joinToString(";"),
        input.getNames().joinToString(separator = "\n") {
            listOf(
                it,
                analytics.joinToString(";") { a -> a.extractor(input, it).toString() }
            ).joinToString(";")
        }
    ).joinToString("\n")

    //endregion

    //region [Data retrieval]
    private fun getVideoExportData(input: ExportDataInput): VideoMap {
        val mappedVideoData = VideoMap()

        getDataForAnalytics(input.videos).let {
            val analytics = videoAnalyticsQuery.getAnalytics(it)
            mappedVideoData.asl = analytics.asl

            return@let analytics.videoAnalytics
        }.forEach {
            mappedVideoData.putName(it.name)
            mappedVideoData.putFrameCount(it.name, it.frameCount)
            mappedVideoData.putComplexity(it.name, it.complexity)
            mappedVideoData.putFramerate(it.name, it.framerate)
            mappedVideoData.putRuntime(it.name, it.runtime)
            mappedVideoData.putTotalPoints(it.name, it.totalPoints)
            mappedVideoData.putUniqueTags(it.name, it.uniqueTags)
        }

        return mappedVideoData
    }

    private fun getTagExportData(input: ExportDataInput): TagMap {
        val tagData = input.videos.flatMap {
            tagsQuery.getAllOnVideo(it)
        }.toSet().toList()

        val mappedTagData = TagMap()

        getDataForAnalytics(input.videos).let {
            tagAnalyticsQuery.getAnalytics(tagData, it).tagAnalytics
        }.forEach {
            mappedTagData.putName(it.name)
            mappedTagData.putValue(it.name, it.value)
            mappedTagData.putTotalPoints(it.name, it.totalPoints)
            mappedTagData.putAmount(it.name, it.amountUsed)
        }

        return mappedTagData
    }

    private fun getDataForAnalytics(data: List<VideoDTO>): List<VideoDataDTO> {
        val groupedData = frameQuery.getAllFramesOnVideos(data).groupBy { it.video }
        return data.associateWith { video ->
            groupedData[video] ?: emptyList()
        }.map {
            VideoDataDTO(it.key, it.value)
        }
    }
    //endregion
}

data class VideoMap @JsonCreator constructor (
    private val nameList: ArrayList<String> = ArrayList(),
    private val frameCountMap: HashMap<String, Int> = HashMap(),
    private val framerateMap: HashMap<String, Double> = HashMap(),
    private val runtimeMap: HashMap<String, Double> = HashMap(),
    private val uniqueTagsMap: HashMap<String, Int> = HashMap(),
    private val totalPointsMap: HashMap<String, Double> = HashMap(),
    private val complexityMap: HashMap<String, Double> = HashMap(),
    var asl: Double = 0.0
)
{
    fun putName(name: String) {
        nameList.add(name)
    }

    fun getNames() = nameList

    fun putFrameCount(name: String, frameCount: Int) {
        frameCountMap[name] = frameCount
    }

    fun getFrameCount(name: String): Int = frameCountMap[name] ?: 0

    fun getTotalFrameCount(): Int = frameCountMap.values.sum()

    fun putFramerate(name: String, framerate: Double) {
        framerateMap[name] = framerate
    }

    fun getFramerate(name: String): Double = framerateMap[name] ?: 0.0

    fun putRuntime(name: String, runtime: Double) {
        runtimeMap[name] = runtime
    }

    fun getRuntime(name: String): Double = runtimeMap[name] ?: 0.0

    fun putUniqueTags(name: String, tags: Int) {
        uniqueTagsMap[name] = tags
    }

    fun getTotalRuntime(): Double = runtimeMap.values.sum()

    fun getUniqueTags(name: String): Int = uniqueTagsMap[name] ?: 0

    fun putTotalPoints(name: String, points: Double) {
        totalPointsMap[name] = points
    }

    fun getTotalPoints(name: String): Double = totalPointsMap[name] ?: 0.0

    fun getTotalPoints(): Double = totalPointsMap.values.sum()

    fun putComplexity(name: String, complexity: Double) {
        complexityMap[name] = complexity
    }

    fun getComplexity(name: String): Double = complexityMap[name] ?: 0.0

    fun getTotalComplexity(): Double = complexityMap.values.sum()
}

data class TagMap @JsonCreator constructor(
    private val nameList: ArrayList<String> = ArrayList(),
    private val valueMap: HashMap<String, Double> = HashMap(),
    private val amountMap: HashMap<String, Int> = HashMap(),
    private val totalPointsMap: HashMap<String, Double> = HashMap(),
)
{
    fun putName(name: String) {
        nameList.add(name)
    }

    fun getNames() = nameList

    fun putValue(name: String, value: Double) {
        valueMap[name] = value
    }

    fun getValue(name: String): Double = valueMap[name] ?: 0.0

    fun putAmount(name: String, value: Int) {
        amountMap[name] = value
    }

    fun getAmount(name: String): Int = amountMap[name] ?: 0

    fun putTotalPoints(name: String, points: Double) {
        totalPointsMap[name] = points
    }

    fun getTotalPoints(name: String): Double = totalPointsMap[name] ?: 0.0
}

