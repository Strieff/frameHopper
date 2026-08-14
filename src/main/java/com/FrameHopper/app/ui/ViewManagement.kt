package com.FrameHopper.app.ui

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

enum class UIFlag(val fileName: String, val windowName: String, val singleInstance: Boolean = true) {
    CHARTS("NewChartsViewModel", "charts.stage"),
    EXPORT("ExportViewModel", "export.stage"),
    FRAME_TAG_MANAGER("FrameTagManagerViewModel", "ftm.stage"),
    IMPORT_CHARTS("ImportChartViewModel", "charts.import.stage", false),
    MAIN("MainViewModel", "main.stage"),
    NOTES("NotesViewModel", "notes.stage"),
    SETTINGS("SettingsViewModel", "settings.stage"),
    TAG_DETAILS("TagDetailsViewModel", "td.edit.stage", false),
    TAG_MANAGER("TagManagerViewModel", "tm.stage"),
    VIDEO_LIST("VideoManagementListViewModel", "vl.stage"),
    VIDEO_DETAILS("VideoManagementDetailsViewModel", "vd.stage", false),
}