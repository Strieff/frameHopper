package com.FrameHopper.app.ui.ve

import com.FrameHopper.app.boundry.dto.export.ChosenTagAnalytics
import com.FrameHopper.app.boundry.dto.export.ChosenVideoAnalytics
import com.FrameHopper.app.ui.language.I18n
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty

abstract class ExportActionEntry {
    @JvmField
    val selectedProperty: BooleanProperty = SimpleBooleanProperty(true)

    fun isSelected() = selectedProperty.get()

    fun setSelected(selected: Boolean) {
        this.selectedProperty.set(selected)
    }

    abstract fun getLabel(): String
}

class VideoExportActionEntry(val analytics: ChosenVideoAnalytics) : ExportActionEntry() {
    override fun getLabel(): String = I18n.tr(analytics.label)
}

class TagExportActionEntry(val analytics: ChosenTagAnalytics) : ExportActionEntry() {
    override fun getLabel(): String = I18n.tr(analytics.label)
}