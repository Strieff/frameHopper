package com.FrameHopper.app.ui.utils

import com.FrameHopper.app.ui.dialog.FXDialogProvider
import com.FrameHopper.app.ui.dialog.FileChooserProvider
import com.FrameHopper.app.ui.language.I18n.tr
import com.FrameHopper.app.ui.settings.UserSettingsAdapter
import javafx.embed.swing.SwingFXUtils
import javafx.scene.SnapshotParameters
import javafx.scene.image.WritableImage
import javafx.scene.web.WebView
import javafx.stage.Stage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tech.tablesaw.api.DoubleColumn
import tech.tablesaw.api.StringColumn
import tech.tablesaw.api.Table
import tech.tablesaw.plotly.components.Axis
import tech.tablesaw.plotly.components.Figure
import tech.tablesaw.plotly.components.Layout
import tech.tablesaw.plotly.components.Marker
import tech.tablesaw.plotly.components.Page
import tech.tablesaw.plotly.components.TickSettings
import tech.tablesaw.plotly.traces.AbstractTrace
import tech.tablesaw.plotly.traces.BarTrace
import tech.tablesaw.plotly.traces.ScatterTrace
import java.io.File
import javax.imageio.ImageIO

object ChartUtils {
    private const val DATA_COLUMN_NAME = "DATA"
    private const val VALUE_COLUMN_NAME = "VALUE"

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

    fun createChartHtml(
        headers: Pair<String, String>,
        data: List<Pair<String, Double>>,
        showMean: Boolean,
        colorBars: Boolean,
        tick: Int
    ) : String
    {
        val header = headers.second
        val tableData = data.let { p ->
            val labels = p.map { it.first }
            val vals = p.map { it.second }

            Table.create(
                header,
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

        val traces = ArrayList<AbstractTrace>()
        val stringColumn = tableData.stringColumn(DATA_COLUMN_NAME)
        val doubleColumn = tableData.doubleColumn(VALUE_COLUMN_NAME)
        val mean = doubleColumn.mean()

        if (colorBars) {
            DoubleArray(doubleColumn.size()) { index ->
                val value = doubleColumn[index]

                if (value >= mean) {
                    value
                } else {
                    Double.NaN
                }
            }.let {
                DoubleColumn.create(
                    tr("charts.legend.green", header),
                    *it
                )
            }.let {
                BarTrace.builder(
                    stringColumn,
                    it
                )
                    .name(tr("charts.legend.green", header))
                    .marker(
                        Marker.builder()
                            .color("#2ca02c")
                            .build()
                    )
                    .build()
            }.run {
                traces.add(this)
            }

            DoubleArray(doubleColumn.size()) { index ->
                val value = doubleColumn[index]

                if (value < mean) {
                    value
                } else {
                    Double.NaN
                }
            }.let {
                DoubleColumn.create(
                    tr("charts.legend.red", header),
                    *it
                )
            }.let {
                BarTrace.builder(
                    stringColumn,
                    it
                )
                    .name(tr("charts.legend.red", header))
                    .marker(
                        Marker.builder()
                            .color("#d62728")
                            .build()
                    )
                    .build()
            }.run {
                traces.add(this)
            }
        } else {
            DoubleArray(doubleColumn.size()) { index ->
                doubleColumn[index]
            }.let {
                DoubleColumn.create(
                    header,
                    *it
                )
            }.let {
                BarTrace.builder(
                    stringColumn,
                    it
                )
                    .name(header)
                    .marker(
                        Marker.builder()
                            .color("#4A90B8")
                            .build()
                    )
                    .build()
            }.run {
                traces.add(this)
            }
        }

        if (showMean) {
            DoubleColumn.create(tr("charts.legend.mean")).also { column ->
                repeat(stringColumn.count()) {
                    column.append(mean)
                }
            }.let {
                ScatterTrace.builder(
                    stringColumn,
                    it
                )
                    .mode(ScatterTrace.Mode.LINE)
                    .name(tr("charts.legend.mean"))
                    .line(
                        tech.tablesaw.plotly.components.Line.builder()
                            .color("black")
                            .width(2.0)
                            .dash(tech.tablesaw.plotly.components.Line.Dash.DASH)
                            .build()
                    )
                    .build()
            }.run {
                traces.add(this)
            }
        }

        return Layout.builder()
            .title(header)
            .showLegend(true)
            .hoverMode(Layout.HoverMode.CLOSEST)
            .yAxis(
                Axis.builder()
                    .tickSettings(
                        TickSettings.builder()
                            .dTick(tick)
                            .build()
                    )
                    .build()
            )
            .build()
            .let {
                Figure(
                    it,
                    *traces.toTypedArray()
                )
            }.let {
                Page
                    .pageBuilder(it, "plotDiv")
                    .build()
                    .asJavascript()
                    .replace(
                        PLOTLY_DEFAULT_STYLE,
                        PLOTLY_OVERLAY_STYLE
                    )
                    .replace(
                        PLOTLY_NEW_PLOT,
                        PLOTLY_CUSTOM_NEW_PLOT
                    )
                    .replace(
                        PLOTLY_ORIGINAL_SRC,
                        getPlotlySource()
                    )
            }
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

    fun savePlotAsImage(
        scope: CoroutineScope,
        webView: WebView,
        userSettingsAdapter: UserSettingsAdapter
    ) {
        scope.launch {
            try {
                WritableImage(webView.width.toInt(), webView.height.toInt()).run {
                    webView.snapshot(SnapshotParameters(), this)
                    val file: File = FileChooserProvider.locationFileSaveChooser(
                        webView.scene.window as Stage,
                        ".png",
                        if (userSettingsAdapter.useRecentExportPath()) userSettingsAdapter.recentExportPath else ""
                    ).let { path ->
                        File(path)
                    }
                    ImageIO.write(SwingFXUtils.fromFXImage(this, null), "png", file)
                    FXDialogProvider.messageDialog(tr("charts.dialog.image-save.success"))
                }
            } catch (e: Exception) {
                FXDialogProvider.errorDialog(tr("charts.dialog.image-save.error"))
                e.printStackTrace()
            }
        }
    }
}