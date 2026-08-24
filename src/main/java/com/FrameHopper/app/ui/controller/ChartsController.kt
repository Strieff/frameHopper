package com.FrameHopper.app.ui.controller

import com.FrameHopper.app.boundry.dto.FrameDTO
import com.FrameHopper.app.boundry.dto.TagDTO
import com.FrameHopper.app.boundry.dto.VideoDTO
import com.FrameHopper.app.boundry.dto.analytics.VideoDataDTO
import com.FrameHopper.app.core.application.analytics.VideoAnalyticsQuery
import com.FrameHopper.app.core.ports.`in`.frame.FrameQuery
import com.FrameHopper.app.core.ports.`in`.video.VideoQuery
import com.FrameHopper.app.ui.UIFlag
import com.FrameHopper.app.ui.UIManager
import com.FrameHopper.app.ui.UiView
import com.FrameHopper.app.ui.dialog.FXDialogProvider
import com.FrameHopper.app.ui.dialog.FileChooserProvider
import com.FrameHopper.app.ui.eventing.*
import com.FrameHopper.app.ui.language.I18n.localeProperty
import com.FrameHopper.app.ui.language.I18n.tr
import com.FrameHopper.app.ui.settings.UserSettingsAdapter
import com.FrameHopper.app.ui.utils.ChartUtils
import com.FrameHopper.app.ui.utils.FXIconLoader
import com.FrameHopper.app.ui.utils.SearchUtils
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.control.cell.CheckBoxTableCell
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.text.Text
import javafx.scene.web.WebView
import javafx.stage.Stage
import javafx.util.Callback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileReader
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.imageio.ImageIO

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class ChartsController(
    private val frameQuery: FrameQuery,
    private val videoQuery: VideoQuery,
    private val videoAnalyticsQuery: VideoAnalyticsQuery,
    private val userSettingsAdapter: UserSettingsAdapter,
    private val uiManager: UIManager
) : UiView(),
    VideoPathUpdatedListener,
    TagDeletedEventListener,
    TagUpdatedEventListener,
    FrameUpdatedEventListener,
    VideoDeletedEventListener
{
    companion object {
        private const val EMPTY_HTML = """
            <html>
            </html>
        """
        private const val UTF8_BOM = "\uFEFF"
    }

    @FXML
    lateinit var tickField: TextField

    @FXML
    lateinit var tickLabel: Label

    @FXML
    lateinit var chartView: BorderPane

    @FXML
    lateinit var importButtonIcon: ImageView

    @FXML
    lateinit var exportButtonIcon: ImageView

    @FXML
    lateinit var clearButtonIcon: ImageView

    @FXML
    lateinit var saveButtonIcon: ImageView

    @FXML
    private lateinit var videoTable: TableView<NewChartsTableEntry>

    @FXML
    private lateinit var selectColumn: TableColumn<NewChartsTableEntry, Boolean>

    @FXML
    private lateinit var nameColumn: TableColumn<NewChartsTableEntry, String>

    @FXML
    private lateinit var yAxisOptions: ComboBox<NewChartsActionEntry>

    @FXML
    private lateinit var meanCheckbox: CheckBox

    @FXML
    private lateinit var colorMean: CheckBox

    @FXML
    private lateinit var generateButton: Button

    @FXML
    private lateinit var searchButton: Button

    @FXML
    private lateinit var webView: WebView

    @FXML
    private lateinit var searchField: TextField

    @FXML
    private lateinit var yAxisLabel: Label

    private val cachedVideoList = FXCollections.observableArrayList(cacheData())
    private val uiScope = MainScope()
    private val chartExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val localeProperty: ObjectProperty<Locale> = localeProperty()

    init {
        VideoPathUpdatedEventDispatcher.register(this)
        VideoDeletedEventDispatcher.register(this)
        TagDeletedEventDispatcher.register(this)
        TagUpdatedEventDispatcher.register(this)
        FrameUpdatedEventDispatcher.register(this)
    }

    @FXML
    fun initialize() {
        bind(searchField, "charts.search-prompt")
        videoTable.items = cachedVideoList
        selectColumn.setCellValueFactory { cellData: TableColumn.CellDataFeatures<NewChartsTableEntry, Boolean> ->
            cellData.getValue().selected
        }
        selectColumn.cellFactory = CheckBoxTableCell.forTableColumn(selectColumn)

        bind(nameColumn, "charts.table.name")
        nameColumn.setCellValueFactory { it.value.name }
        nameColumn.setCellFactory { c: TableColumn<NewChartsTableEntry, String> ->
            object : TableCell<NewChartsTableEntry, String>() {
                private val text = Text()

                init {
                    text.wrappingWidthProperty().bind(c.widthProperty().subtract(10)) // padding
                    graphic = text
                }

                override fun updateItem(item: String?, empty: Boolean) {
                    super.updateItem(item, empty)
                    text.setText(if (empty || item == null) null else item)
                }
            }
        }

        meanCheckbox.onMouseClicked = EventHandler { _ -> generateChart() }
        bind(meanCheckbox, "charts.chart-options.show-mean")

        colorMean.onMouseClicked = EventHandler { _: MouseEvent -> generateChart() }
        bind(colorMean, "charts.chart-options.color-mean")

        generateButton.onMouseClicked = EventHandler { _: MouseEvent -> generateChart() }
        bind(generateButton, "charts.button.generate")

        bind(yAxisLabel, "charts.y-axis-options.label")
        bind(yAxisOptions as ComboBoxBase<Any>, "charts.y-axis-options.prompt")
        yAxisOptions.items.addAll(NewChartsOption.entries.map { NewChartsActionEntry(it, videoAnalyticsQuery) })
        yAxisOptions.selectionModel.selectedItemProperty().addListener { _, _, newValue -> if(newValue != null) generateChart() }
        yAxisOptions.cellFactory = Callback { createChartOptionCell() }
        yAxisOptions.buttonCell = createChartOptionCell()

        bind(tickLabel, "charts.chart-options.ticks")

        exportButtonIcon.image = FXIconLoader.getLargeIcon("export.png")
        importButtonIcon.image = FXIconLoader.getLargeIcon("import.png")
        clearButtonIcon.image = FXIconLoader.getLargeIcon("clean.png")
        saveButtonIcon.image = FXIconLoader.getLargeIcon("save.png")

        localeProperty.addListener { Platform.runLater { generateChart() } }

        addKeybinds()

        Platform.runLater {
            (chartView.scene.window as Stage).apply {
                onCloseRequest = EventHandler { this@ChartsController.close() }
            }
        }
    }

    //region [SET UP]

    private fun cacheData(): List<NewChartsTableEntry> {
        val videos = videoQuery.getAllVideos()
        val framesByVideoId = frameQuery.all
            .filterNotNull()
            .groupBy { it.video }

        return videos
            .map { NewChartsTableEntry(it, framesByVideoId[it].orEmpty()) }
            .onEach { it.selected.addListener { _, _, _ -> generateChart()} }
    }

    private fun createChartOptionCell(): ListCell<NewChartsActionEntry> =
        object : ListCell<NewChartsActionEntry>() {
            override fun updateItem(
                item: NewChartsActionEntry?,
                empty: Boolean
            ) {
                super.updateItem(item, empty)

                textProperty().unbind()

                if (empty || item == null) {
                    text = null
                } else {
                    textProperty().bind(item.labelProperty)
                }
            }
        }

    //endregion

    //region [GENERATE CHART]

    @FXML
    fun generateChart() {
        uiScope.launch {
            generateChartAsync()
        }
    }

    private suspend fun generateChartAsync() {
        val option = yAxisOptions.selectionModel.selectedItem ?: return
        val videos = cachedVideoList.filter { it.selected.value }.ifEmpty {
            webView.engine.loadContent(EMPTY_HTML)
            return
        }

        val data = videos.map { Pair(it.video.name, option.extractValue(it.video, it.frames).toDouble()) }
        val ticks = tickField.text.toIntOrNull() ?: 10
        val colors = colorMean.isSelected
        val mean = meanCheckbox.isSelected

        val html = withContext(Dispatchers.Default) {
            ChartUtils.createChartHtml(
                headers = Pair("", option.labelProperty.value),
                data = data,
                showMean = mean,
                colorBars = colors,
                tick = ticks
            )
        }

        webView.engine.isJavaScriptEnabled = true
        webView.engine.loadContent(html)
    }

    //endregion

    fun handleImport() {
        FileChooserProvider.textFileChooser(chartView.scene.window as Stage).let {
            FileReader(it, Charsets.UTF_8).readText()
        }
            .trimIndent()
            .split("\n")
            .map { it.split(";") }
            .takeIf { it.all { e -> e.size == 2 } }
            ?.let {
                it.map { l -> l[0] to l[1] }
            }
            ?.run {
                val header = this.first()
                val data = this.drop(1)

                uiManager.open(UIFlag.IMPORT_CHARTS, chartView).let {
                    it.getController() as ImportChartController
                }.init(
                    header,
                    data.map { Pair(it.first, it.second.toDouble()) },
                    meanCheckbox.isSelected,
                    colorMean.isSelected,
                    tickField.text.toIntOrNull() ?: 10
                 )
            }
            ?: run {
                FXDialogProvider.messageDialog(tr("charts.dialog.csv-import.fail"))
            }
    }

    fun handleExport() {
        val option = yAxisOptions.selectionModel.selectedItem ?: return
        uiScope.launch {
            try {
                FileChooserProvider.locationFileSaveChooser(
                    chartView.scene.window as Stage,
                    ".csv",
                    if (userSettingsAdapter.useRecentExportPath()) userSettingsAdapter.recentExportPath else ""
                ).let { path ->
                    File(path)
                }.run {
                    writeText(
                        listOf(
                            "${UTF8_BOM}${tr("charts.export.name")};${option.labelProperty.value}",
                            cachedVideoList.associate { it.video to option.extractValue(it.video, it.frames).toDouble() }
                                .map { "${it.key.name};${it.value}" }
                                .joinToString(separator = "\n")
                        ).joinToString(separator = "\n")
                    )
                }
                FXDialogProvider.messageDialog(tr("charts.dialog.csv-save.success"))
            } catch (e: Exception) {
                FXDialogProvider.errorDialog(tr("charts.dialog.csv-save.error"))
                e.printStackTrace()
            }
        }
    }

    fun handleClear() {
        cachedVideoList.forEach {
            it.selected.value = false
        }.also {
            generateChart()
        }
    }

    fun handleSave() {
        ChartUtils.savePlotAsImage(uiScope, webView, userSettingsAdapter)
    }

    fun handleSearch() {
        SearchUtils.handleSearch(
            searchButton,
            searchField,
            videoTable,
            cachedVideoList,
        ) { list, query ->
            list.filtered { it?.video?.name?.contains(query, true) == true }
        }
    }

    override fun close() {
        chartExecutor.shutdown()

        VideoPathUpdatedEventDispatcher.unregister(this)
        VideoDeletedEventDispatcher.unregister(this)
        TagUpdatedEventDispatcher.unregister(this)
        TagDeletedEventDispatcher.unregister(this)
        FrameUpdatedEventDispatcher.unregister(this)

        uiManager.close(UIFlag.CHARTS)
        (chartView.scene.window as Stage).close()
    }

    override fun addKeybinds() {
        keyActions[KeyCodeCombination(KeyCode.C, KeyCombination.SHIFT_DOWN)] = Runnable { this.close() }
        keyActions[KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN)] = Runnable { this.handleSave() }
        keyActions[KeyCodeCombination(KeyCode.I, KeyCombination.SHIFT_DOWN)] = Runnable { this.handleImport() }
        keyActions[KeyCodeCombination(KeyCode.E, KeyCombination.SHIFT_DOWN)] = Runnable { this.handleExport() }
        keyActions[KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN)] = Runnable { this.handleClear() }
        keyActions[KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN)] = Runnable { this.handleSearch() }

        addEventFilter(chartView)
    }

    override fun onTagUpdated(tag: TagDTO) {
        uiScope.launch {
            cacheData()
            generateChartAsync()
        }
    }

    override fun onTagUpdated(tags: List<TagDTO>) {
        uiScope.launch {
            cacheData()
            generateChartAsync()
        }
    }

    override fun onTagDeleted(tag: TagDTO) {
        uiScope.launch {
            cacheData()
            generateChartAsync()
        }
    }

    override fun onTagDeleted(tags: List<TagDTO>) {
        uiScope.launch {
            cacheData()
            generateChartAsync()
        }
    }

    override fun onFrameUpdate(frameNumber: Int, frame: FrameDTO?) {
        uiScope.launch {
            cacheData()
            generateChartAsync()
        }
    }

    override fun onDeleteVideo(video: VideoDTO) {
        cachedVideoList.firstOrNull { it.video == video }?.run {
            cachedVideoList.remove(this)
            uiScope.launch {
                generateChartAsync()
            }
        }
    }

    override fun onVideoPathUpdated(video: VideoDTO) {
        cachedVideoList.firstOrNull { it.video == video }?.run {
            this.updateVideo(video)
            if (this.selected.value)
                generateChart()
        }
    }
}

//region [Chart Elements]

class NewChartsTableEntry(
    var video: VideoDTO,
    var frames: List<FrameDTO>
)
{
    val name: StringProperty = SimpleStringProperty(video.name)
    val selected: BooleanProperty = SimpleBooleanProperty(false)

    fun isSelected(): Boolean = selected.get()

    fun setSelected(selected: Boolean) {
        this.selected.set(selected)
    }

    fun updateVideo(video: VideoDTO) {
        this.video = video
        name.set(video.name)
    }
}

enum class NewChartsOption(
    val label: String,
    val extractor: (VideoAnalyticsQuery, VideoDTO, List<FrameDTO>) -> Number
)
{
    COMPLEXITY(
        label = "charts.y-axis-options.complexity",
        extractor = { query, video, frames ->
            query.getComplexity(VideoDataDTO(video, frames)).data
        }
    ),
    UNIQUE_TAGS(
        label = "charts.y-axis-options.unique-tags",
        extractor = { query, video, frames ->
            query.getUniqueTagsCount(VideoDataDTO(video, frames)).data
        }
    ),
    FRAME_COUNT(
        label = "charts.y-axis-options.total-frame-count",
        extractor = { query, video, frames ->
            query.getFrameCount(VideoDataDTO(video, frames)).data
        }
    ),
    DURATION(
        label = "charts.y-axis-options.duration",
        extractor = { query, video, frames ->
            query.getRuntime(VideoDataDTO(video, frames)).data
        }
    ),
    TOTAL_POINTS(
        label = "charts.y-axis-options.total-points",
        extractor = { query, video, frames ->
            query.getTotalPoints(VideoDataDTO(video, frames)).data
        }
    )
}

class NewChartsActionEntry(
    private val option: NewChartsOption,
    private val query: VideoAnalyticsQuery
)
{
    val labelProperty: StringProperty = SimpleStringProperty()

    init {
        labelProperty.bind(
            Bindings.createStringBinding(
                { tr(option.label) },
                localeProperty()
            )
        )
    }

    fun extractValue(video: VideoDTO, frames: List<FrameDTO>): Number = option.extractor(query, video, frames)
}

//endregion