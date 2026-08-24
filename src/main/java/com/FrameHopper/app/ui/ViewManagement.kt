package com.FrameHopper.app.ui

import com.FrameHopper.app.ui.dialog.FXDialogProvider
import com.FrameHopper.app.ui.language.I18n
import jakarta.annotation.PostConstruct
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.stage.Stage
import org.springframework.stereotype.Component
import java.util.EnumMap

@Component
open class UIManager {
    val flags: EnumMap<UIFlag, Boolean> = EnumMap(UIFlag::class.java)

    @PostConstruct
    fun init() = UIFlag.entries.forEach { flag -> flags[flag] = false }

    fun isOpen(flag: UIFlag): Boolean = flags[flag] == true

    fun open(flag: UIFlag, node: Node): FXMLLoader {
        if (flag.singleInstance && isOpen(flag)) {
            FXDialogProvider.errorDialog(I18n.tr(flag.errorMessage.orEmpty()))
            throw Exception(flag.errorMessage)
        }

        val loader = FXMLViewLoader.getView(flag.fileName, flag.windowName, node)
        if (flag.singleInstance)
            flags[flag] = true

        return loader
    }

    fun openMain(primaryStage: Stage): FXMLLoader = FXMLViewLoader.getMainView(UIFlag.MAIN.fileName, UIFlag.MAIN.windowName, primaryStage)

    fun close(flag: UIFlag) {
        flags[flag] = false
    }
}

enum class UIFlag(
    val fileName: String,
    val windowName: String,
    val singleInstance: Boolean = true,
    val errorMessage: String? = null,
)
{
    CHARTS(
        fileName = "ChartsViewModel",
        windowName = "charts.stage",
        errorMessage = "charts.error.already-open"
    ),
    EXPORT(
        fileName = "ExportViewModel",
        windowName = "export.stage",
        errorMessage = "export.error.already-open"
    ),
    FRAME_TAG_MANAGER(
        fileName = "FrameTagManagerViewModel",
        windowName = "ftm.stage",
        errorMessage = "ftm.error.already-open"
    ),
    IMPORT_CHARTS(
        fileName = "ImportChartViewModel",
        windowName = "charts.import.stage",
        singleInstance = false
    ),
    MAIN(
        fileName = "MainViewModel",
        windowName = "main.stage",
    ),
    NOTES(
        fileName = "NotesViewModel",
        windowName = "notes.stage",
        errorMessage = "notes.error.already-open"
    ),
    SETTINGS(
        fileName = "SettingsViewModel",
        windowName = "settings.stage",
        errorMessage = "settings.error.already-open"
    ),
    TAG_DETAILS(
        fileName = "TagDetailsViewModel",
        windowName = "td.edit.stage",
        singleInstance = false
    ),
    TAG_MANAGER(
        fileName = "TagManagerViewModel",
        windowName = "tm.stage",
        errorMessage = "tm.error.already-open"
    ),
    VIDEO_LIST(
        fileName = "VideoManagementListViewModel",
        windowName = "vl.stage",
        errorMessage = "vl.error.already-open"
    ),
    VIDEO_DETAILS(
        fileName = "VideoManagementDetailsViewModel",
        windowName = "vd.stage",
        singleInstance = false
    ),
}