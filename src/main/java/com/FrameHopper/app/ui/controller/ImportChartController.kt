package com.FrameHopper.app.ui.controller

import com.FrameHopper.app.ui.UiView
import com.FrameHopper.app.ui.language.I18n.localeProperty
import com.FrameHopper.app.ui.settings.UserSettingsAdapter
import com.FrameHopper.app.ui.utils.ChartUtils
import com.FrameHopper.app.ui.utils.FXIconLoader
import javafx.application.Platform
import javafx.beans.property.ObjectProperty
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.BorderPane
import javafx.scene.web.WebView
import javafx.stage.Stage
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.Locale

@Component
@Scope("prototype")
class ImportChartController(
    val userSettingsAdapter: UserSettingsAdapter
) : UiView() {
    @FXML
    lateinit var webView: WebView
    @FXML
    lateinit var chartPane: BorderPane
    @FXML
    lateinit var saveButtonIcon: ImageView

    private lateinit var header: Pair<String, String>
    private lateinit var data: List<Pair<String, Double>>
    private var showMean: Boolean = false
    private var colorMean: Boolean = false
    private var tick: Int = 0
    private val localeProperty: ObjectProperty<Locale> = localeProperty()

    @FXML
    fun initialize() {
        saveButtonIcon.image = FXIconLoader.getLargeIcon("save.png")

        webView.engine.isJavaScriptEnabled = true

        localeProperty.addListener { Platform.runLater { generateChart() } }

        addKeybinds()

        Platform.runLater {
            (chartPane.scene.window as Stage).apply {
                onCloseRequest = EventHandler { this@ImportChartController.close() }
            }
        }
    }

     fun init(
        header: Pair<String, String>,
        data: List<Pair<String, Double>>,
        showMean: Boolean,
        colorBars: Boolean,
        tick: Int
    )
     {
         this.header = header
         this.data = data
         this.showMean = showMean
         this.colorMean = colorBars
         this.tick = tick

         generateChart()
     }

    private fun generateChart() {
        MainScope().launch {
            val html = ChartUtils.createChartHtml(
                header,
                data,
                showMean,
                colorMean,
                tick,
                height = webView.height.toInt(),
                width = webView.width.toInt()
            )

            webView.engine.loadContent(html)
        }
    }

    override fun addKeybinds() {
        keyActions[KeyCodeCombination(KeyCode.C, KeyCombination.SHIFT_DOWN)] = Runnable { this.close() }
        keyActions[KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN)] = Runnable { this.handleSave() }

        addEventFilter(chartPane)
    }

    fun handleSave() {
        ChartUtils.savePlotAsImage(MainScope(), webView, userSettingsAdapter)
    }

    override fun close() {
        (chartPane.scene.window as Stage).close()
    }
}