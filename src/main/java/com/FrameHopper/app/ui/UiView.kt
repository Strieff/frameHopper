package com.FrameHopper.app.ui

import com.FrameHopper.app.ui.language.I18n
import javafx.scene.Node
import javafx.scene.control.ComboBoxBase
import javafx.scene.control.Labeled
import javafx.scene.control.TableColumn
import javafx.scene.control.TextInputControl
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyEvent

abstract class UiView {
    @JvmField
    val keyActions: MutableMap<KeyCombination, Runnable> = mutableMapOf()

    protected fun handleKeyPressed(event: KeyEvent) {
        if(isTyping(event)) return

        keyActions.keys
            .find { it.match(event) }
            ?.let { keyActions[it]?.run() }
    }

    private fun isTyping(event: KeyEvent): Boolean {
        val target: Any? = event.getTarget()

        return target is TextInputControl
    }

    protected fun addEventFilter(node: Node) = node.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed)

    protected fun bind(node: Labeled, key: String, vararg args: Any) = node.textProperty().bind(I18n.bind(key, *args))

    protected fun bind(node: ComboBoxBase<Any>, key: String, vararg args: Any) = node.promptTextProperty().bind(I18n.bind(key, *args))

    protected fun bind(node: TextInputControl, key: String, vararg args: Any) = node.promptTextProperty().bind(I18n.bind(key, *args))

    protected fun bind(column: TableColumn<*, *>, key: String, vararg args: Any) = column.textProperty().bind(I18n.bind(key, *args))

    protected fun getText(key: String, vararg args: Any): String = I18n.tr(key, *args)

    protected abstract fun addKeybinds()
    protected abstract fun close()
}