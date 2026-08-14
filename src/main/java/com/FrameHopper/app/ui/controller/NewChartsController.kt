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
import com.FrameHopper.app.ui.eventing.*
import com.FrameHopper.app.ui.language.I18n.localeProperty
import com.FrameHopper.app.ui.language.I18n.tr
import com.FrameHopper.app.ui.settings.UserSettingsAdapter
import com.FrameHopper.app.ui.utils.FXIconLoader
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.concurrent.Worker
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
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import tech.tablesaw.api.DoubleColumn
import tech.tablesaw.api.NumberColumn
import tech.tablesaw.api.StringColumn
import tech.tablesaw.api.Table
import tech.tablesaw.plotly.components.*
import tech.tablesaw.plotly.traces.BarTrace
import tech.tablesaw.plotly.traces.ScatterTrace
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.floor

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class NewChartsController(
    private val frameQuery: FrameQuery,
    private val videoQuery: VideoQuery,
    private val videoAnalyticsQuery: VideoAnalyticsQuery,
    private val userSettingsAdapter: UserSettingsAdapter,
    private val uiManager: UIManager
) : UiView(),
    VideoPathUpdatedListener,
    TagDeletedEventListener,
    TagCreatedEventListener,
    TagUpdatedEventListener,
    FrameUpdatedEventListener,
    VideoDeletedEventListener,
    ShowHiddenEventListener
{
    companion object {
        private const val PLOTLY_DEFAULT_STYLE = "showlegend: true,"
        private val PLOTLY_OVERLAY_STYLE = """
            showlegend: true,
            barmode: 'overlay',
            hovermode: 'closest',
        """.trimIndent()

        private const val PLOTLY_NEW_PLOT =  "Plotly.newPlot(target_plotDiv, data, layout);"
        private val PLOTLY_CUSTOM_NEW_PLOT = """
            Plotly.newPlot(
                target_plotDiv,
                data,
                layout,
                {
                    modeBarButtons: [[
                        'zoomIn2d',
                        'zoomOut2d',
                        'autoScale2d',
                        'hoverClosestCartesian',
                        'hoverCompareCartesian'
                    ]],
                    displaylogo: false
                }
            );
        """.trimIndent()

        private const val PLOTLY_ORIGINAL_SRC = "<script src=\"https://cdn.plot.ly/plotly-latest.min.js\"></script>"

        private const val DATA_COLUMN_NAME = "DATA"
        private const val VALUE_COLUMN_NAME = "VALUE"
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

    private val chartExecutor: ExecutorService =
        Executors.newSingleThreadExecutor()

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

        meanCheckbox.onMouseClicked = EventHandler { _ -> generateChart() } //TODO: INSTEAD OF GENERATE CHART USE UPDATE CHART
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

        loadChartSkeleton()
        //generateChart()

        addKeybinds()

        Platform.runLater {
            val stage = chartView.scene.window as Stage
            stage.onCloseRequest = EventHandler { close() }
        }
    }

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

    fun loadChartSkeleton() {
        val skeletonHtml = this::class.java.getResource("/html/PlotlySkeleton.html")
            ?.readText()
            ?.trimIndent()
            ?.replace("%s", getPlotlySource())
        println(skeletonHtml)
        webView.engine.isJavaScriptEnabled = true
        webView.engine.loadWorker.stateProperty().addListener { _, _, state ->
            if (state == Worker.State.SUCCEEDED) {
                println("Plotly Skeleton loaded")
            }
        }
        webView.engine.loadContent(skeletonHtml, "text/html; charset=utf-8")
    }

    fun updateChart() {
        val option = yAxisOptions.selectionModel.selectedItem ?: return
        val selected = cachedVideoList.filter { it.selected.value }

        val webViewWidth = floor(webView.width).toInt()
        val webViewHeight = floor(webView.height).toInt()

        /*chartExecutor.submit {
            try {

            }
        }*/
    }

    @FXML
    fun generateChart() {
        val webViewWidth = floor(webView.width).toInt()
        val webViewHeight = floor(webView.height).toInt()

        val tableData = createData()

        val stringColumn = tableData.stringColumn(DATA_COLUMN_NAME)
        val doubleColumn = tableData.doubleColumn(VALUE_COLUMN_NAME)

        val mean = doubleColumn.mean()

        val aboveValues = DoubleArray(doubleColumn.size()) { index ->
            val value = doubleColumn[index]

            if (value >= mean) {
                value
            } else {
                Double.NaN
            }
        }

        val belowValues = DoubleArray(doubleColumn.size()) { index ->
            val value = doubleColumn[index]

            if (value < mean) {
                value
            } else {
                Double.NaN
            }
        }


        val aboveColumn = DoubleColumn.create(
            "ABOVE_MEAN",
            *aboveValues
        )

        val belowColumn = DoubleColumn.create(
            "BELOW_MEAN",
            *belowValues
        )


        val aboveTrace = BarTrace.builder(
            stringColumn,
            aboveColumn
        )
            .name("ABOVE MEAN")
            .marker(
                Marker.builder()
                    .color("#2ca02c")
                    .build()
            )
            .build()


        val belowTrace = BarTrace.builder(
            stringColumn,
            belowColumn
        )
            .name("BELOW MEAN")
            .marker(
                Marker.builder()
                    .color("#d62728")
                    .build()
            )
            .build()

        val meanColumn = DoubleColumn.create("MEAN")

        stringColumn.forEach {
            meanColumn.append(mean)
        }

        val meanLineTrace = ScatterTrace.builder(
            stringColumn,
            meanColumn
        )
            .mode(ScatterTrace.Mode.LINE)
            .name("MEAN")
            .line(
                Line.builder()
                    .color("black")
                    .width(2.0)
                    .dash(Line.Dash.DASH)
                    .build()
            )
            .build()

        val layout = Layout.builder()
            .title("WIELKI TEST")
            .showLegend(true)
            .height(webViewHeight)
            .width(webViewWidth)
            .hoverMode(Layout.HoverMode.CLOSEST)
            .yAxis(
                Axis.builder()
                    .tickSettings(
                        TickSettings.builder()
                            .dTick(10.0)
                            .build()
                    )
                    .build()
            )
            .build()


        val figure = Figure(
            layout,
            aboveTrace,
            belowTrace,
            meanLineTrace
        )

        var plotHtml = Page
            .pageBuilder(figure, "plotDiv")
            .build()
            .asJavascript()

        plotHtml = plotHtml.replace(
            PLOTLY_DEFAULT_STYLE,
            PLOTLY_OVERLAY_STYLE
        ).replace(
            PLOTLY_NEW_PLOT,
            PLOTLY_CUSTOM_NEW_PLOT
        ).replace(
            PLOTLY_ORIGINAL_SRC,
            getPlotlySource()
        )

        println(plotHtml)

        webView.engine.isJavaScriptEnabled = true
        webView.engine.loadContent(plotHtml)
    }

    private fun getPlotlySource(): String {
        val localPlotly = File("js/plotly/plotly-3.6.0.min.js")
        return if (localPlotly.exists())
            """
                <script>${localPlotly.readText()}</script>
            """.trimIndent()
        else
            """
                <script src="https://cdn.plot.ly/plotly-3.6.0.min.js"></script>
            """.trimIndent()
    }

    private fun createData(): Table {
        val option = yAxisOptions.selectionModel.selectedItem
        return cachedVideoList.filter { it.selected.value }.let { s ->
            val labels = s.map { it.video.name }
            val vals = s.map { option.extractValue(it.video, it.frames).toDouble() }

            Table.create(
                tr(option.labelProperty.value),
                StringColumn.create(
                    DATA_COLUMN_NAME,
                    labels
                ),
                DoubleColumn.create(
                    VALUE_COLUMN_NAME,
                    vals
                )
            )
        }
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

    override fun onTagDeleted(tag: TagDTO) {
        TODO("Not yet implemented")
    }

    override fun onTagDeleted(tags: List<TagDTO>) {
        TODO("Not yet implemented")
    }

    override fun onTagCreated(tag: TagDTO) {
        TODO("Not yet implemented")
    }

    override fun onTagCreated(tags: List<TagDTO>) {
        TODO("Not yet implemented")
    }

    override fun onTagUpdated(tag: TagDTO) {
        TODO("Not yet implemented")
    }

    override fun onTagUpdated(tags: List<TagDTO>) {
        TODO("Not yet implemented")
    }

    override fun onFrameUpdate(frameNumber: Int, frame: FrameDTO?) {
        TODO("Not yet implemented")
    }

    override fun onDeleteVideo(video: VideoDTO) {
        TODO("Not yet implemented")
    }

    override fun onSHowHiddenUpdated() {
        TODO("Not yet implemented")
    }

    fun handleImport() {
        TODO("Not yet implemented")
    }

    fun handleExport() {
        TODO("Not yet implemented")
    }

    fun handleClear() {
        TODO("Not yet implemented")
    }

    fun handleSave() {
        TODO("Not yet implemented")
    }

    fun handleSearch() {
        TODO("Not yet implemented")
    }

    override fun onVideoPathUpdated(video: VideoDTO) {
        TODO("Not yet implemented")
    }

    override fun close() {
        VideoPathUpdatedEventDispatcher.unregister(this)
        VideoDeletedEventDispatcher.unregister(this)
        TagDeletedEventDispatcher.unregister(this)
        TagUpdatedEventDispatcher.unregister(this)
        FrameUpdatedEventDispatcher.unregister(this)

        uiManager.close(UIFlag.CHARTS)
        (chartView.scene.window as Stage).close()
    }
}

//region [Chart Elements]

class NewChartsTableEntry(
    var video: VideoDTO,
    var frames: List<FrameDTO>
)
{
    val name: StringProperty = SimpleStringProperty(video.name)
    val selected: BooleanProperty = SimpleBooleanProperty(true)

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