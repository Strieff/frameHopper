package com.FrameHopper.app.ui.utils

import com.FrameHopper.app.ui.language.I18n
import com.FrameHopper.app.ui.language.I18n.localeProperty
import com.FrameHopper.app.ui.language.I18n.tr
import javafx.beans.property.StringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeType

object ChartUtils {
    @JvmStatic
    fun getLabel(prefHeight: Double, prefWidth: Double, key: String, observable: StringProperty): Label =
        Label().apply {
            this.prefHeight = prefHeight
            this.prefWidth = prefWidth
            isWrapText = true
            textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                    { tr(key, observable.get()) },
                    localeProperty(),
                    observable
                )
            )
        }

    @JvmStatic
    fun getMeanLabel(prefHeight: Double, prefWidth: Double, key: String): Label =
        Label().apply {
            this.prefHeight = prefHeight
            this.prefWidth = prefWidth
            isWrapText = true
            textProperty().bind(I18n.bind(key))
        }

    @JvmStatic
    fun getLegendBox(
        label: Label, recArc: Double, recSide: Double, recColor: Color,
        boxPrefHeight: Double, boxPrefWidth: Double
    ): HBox {
        val rec = Rectangle(recSide, recSide, recColor).apply {
            arcWidth = recArc
            arcHeight = recArc
            strokeType = StrokeType.INSIDE
        }

        val box: HBox = HBox(2.0).apply {
            prefHeight = boxPrefHeight
            prefWidth = boxPrefWidth
            alignment = Pos.CENTER
        }
        HBox.setMargin(box, Insets(5.0, 0.0, 0.0, 10.0))

        return box.apply {
            children.addAll(rec, label)
        }
    }

    @JvmStatic
    fun getMeanLegendBox(
        label: Label, lineColor: Color, lineWidth: Double,
        boxPrefHeight: Double, boxPrefWidth: Double
    ): HBox {
        val meanLine: Line = Line(0.0, 40.0, 40.0, 40.0).apply {
            stroke = lineColor
            strokeWidth = lineWidth
        }

        val box: HBox = HBox(2.0).apply {
            prefHeight = boxPrefHeight
            prefWidth = boxPrefWidth
            alignment = Pos.CENTER
        }
        HBox.setMargin(box, Insets(5.0, 0.0, 0.0, 10.0))
        return box.apply {
            children.addAll(meanLine,label)
        }
    }

    @JvmStatic
    fun populateTable(container: HBox, vararg boxes: HBox){
        container.children.clear()
        container.children.addAll(boxes)
    }
}