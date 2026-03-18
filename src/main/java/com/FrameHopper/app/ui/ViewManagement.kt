package com.FrameHopper.app.ui

import jakarta.annotation.PostConstruct
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import org.springframework.stereotype.Component
import java.util.EnumMap

@Component
open class UIManager {
    val flags: EnumMap<UIFlag, Boolean> = EnumMap(UIFlag::class.java)

    @PostConstruct
    fun init() {
        UIFlag.entries.forEach { flag -> flags[flag] = false }
    }

    fun isOpen(flag: UIFlag): Boolean = flags[flag] == true

    fun open(flag: UIFlag, node: Node):  FXMLLoader {
        val loader = FXMLViewLoader.getView(flag.fileName, flag.windowName, node)
        if (flag.singleInstance)
            flags[flag] = true

        return loader
    }

    fun close(flag: UIFlag) {
        flags[flag] = false
    }
}

enum class UIFlag(val fileName: String, val windowName: String, val singleInstance: Boolean = true) {
    CHARTS("ChartsViewModel", "CHARTS"),
    EXPORT("ExportViewModel", "EXPORT"),
    FRAME_TAG_MANAGER("FrameTagManagerViewModel", "FRAME TAG MANAGER"),
    IMPORT_CHARTS("ImportChartViewModel", "IMPORT CHART", false),
    MAIN("MainViewModel", "MAIN"),
    NOTES("NotesViewModel", "NOTES"),
    SETTINGS("SettingsViewModel", "SETTINGS"),
    TAG_DETAILS("TagDetailsViewModel", "TAG DETAILS", false),
    TAG_MANAGER("TagManagerViewModel", "TAG MANAGER"),
    VIDEO_LIST("VideoManagementListViewModel", "VIDEO LIST"),
    VIDEO_DETAILS("VideoManagementDetailsViewModel", "VIDEO DETAILS", false),
}