package com.FrameHopper.app.ui.controller

import com.FrameHopper.app.View.Elements.Language.Dictionary
import com.FrameHopper.app.boundry.dto.FrameDTO
import com.FrameHopper.app.boundry.dto.TagDTO
import com.FrameHopper.app.core.ports.`in`.frame.CreateFrameCommand
import com.FrameHopper.app.core.ports.`in`.frame.DeleteFrameCommand
import com.FrameHopper.app.core.ports.`in`.frame.UpdateFrameCommand
import com.FrameHopper.app.core.ports.`in`.tag.TagsQuery
import com.FrameHopper.app.ui.UiView
import com.FrameHopper.app.ui.eventing.FrameUpdatedEventDispatcher
import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.control.cell.CheckBoxTableCell
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import javafx.stage.WindowEvent
import javafx.util.Callback
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class FrameTagManagerController (
    private val tagsQuery: TagsQuery,
    private val createFrameCommand: CreateFrameCommand,
    private val updateFrameCommand: UpdateFrameCommand,
    private val deleteFrameCommand: DeleteFrameCommand
) : UiView {
    @FXML
    private lateinit var searchField: TextField
    @FXML
    private lateinit var searchButton: Button
    @FXML
    private lateinit var cancelButton: Button
    @FXML
    private lateinit var saveButton: Button
    @FXML
    private lateinit var codeTable: TableView<FrameTagManagerTableEntry?>
    @FXML
    private lateinit var selectColumn: TableColumn<FrameTagManagerTableEntry?, Boolean?>
    @FXML
    private lateinit var codeColumn: TableColumn<FrameTagManagerTableEntry?, String?>
    @FXML
    private lateinit var valueColumn: TableColumn<FrameTagManagerTableEntry?, Double?>
    @FXML
    private lateinit var frameLabel: Label
    @FXML
    private lateinit var frameTagManagerView: BorderPane

    private lateinit var cachedFrame: FrameDTO
    private lateinit var tagListCache: ObservableList<FrameTagManagerTableEntry?>

    @FXML
    fun initialize() {
        codeColumn.cellValueFactory = PropertyValueFactory("name")
        codeColumn.text = Dictionary.get("name")
        valueColumn.cellValueFactory = PropertyValueFactory("value")
        valueColumn.text = Dictionary.get("value")
        selectColumn.cellValueFactory = Callback {it.value?.selected}
        selectColumn.cellFactory = CheckBoxTableCell.forTableColumn(selectColumn)

        tagListCache = FXCollections.observableArrayList(
            tagsQuery.getAllTags()?.takeIf { it.isNotEmpty() }
                ?.map(::FrameTagManagerTableEntry)
        )
        codeTable.items = tagListCache

        cancelButton.text = Dictionary.get("cancel")
        cancelButton.setOnAction { _: ActionEvent? -> close() }

        Platform.runLater(Runnable {
            val stage = frameTagManagerView.scene.window as Stage
            stage.onCloseRequest = EventHandler { _: WindowEvent? -> close() }
            frameTagManagerView.requestFocus()
        })
    }

    fun init(frame: FrameDTO) {
        cachedFrame = frame

        frameLabel.text = String.format(Dictionary.get("tm.frame"), (cachedFrame.frameNumber + 1))

        val tags = cachedFrame.tags
        if(tags.isNullOrEmpty()) return

        tags.forEach { t ->
            codeTable.items
                .firstOrNull { it?.tag?.id == t.id }
                ?.setSelected(true)
        }
    }

    @FXML
    fun handleSearch() {
        val query = searchField.text.trim()
        if(query == "" && searchButton.text != "X") return

        if(searchButton.text == "\uD83D\uDD0D"){
            codeTable.items = FXCollections.observableArrayList(tagListCache.filter { it?.tag?.name?.contains(searchField.text, true) == true })
            searchButton.text = "X"
        } else {
            codeTable.items = tagListCache
            searchButton.text = "\uD83D\uDD0D"
            searchField.clear()
        }
    }

    @FXML
    fun save() {
        val selected = codeTable.items.takeIf { it.isNotEmpty() }
            ?.asSequence()
            ?.filter { it!!.selected.value}
            ?.map { it!!.tag }
            ?.toList()

        if(selected.isNullOrEmpty()) {
            if(cachedFrame.id != -1) {
                deleteFrameCommand.deleteFrame(cachedFrame.id)
                FrameUpdatedEventDispatcher.dispatch(cachedFrame.id, null)
            }

            close()
            return
        }

        cachedFrame.tags.apply {
            clear()
            addAll(selected)
        }

        cachedFrame =
            if(cachedFrame.id == -1) createFrameCommand.createFrame(cachedFrame)
            else updateFrameCommand.updateFrame(cachedFrame)

        FrameUpdatedEventDispatcher.dispatch(cachedFrame.id, cachedFrame)
        close()
    }

    @FXML
    override fun close() {
        val stage = frameTagManagerView.scene.window as Stage
        stage.close()
    }
}

class FrameTagManagerTableEntry(val tag: TagDTO) {
    val selected: BooleanProperty = SimpleBooleanProperty(false)
    val name: StringProperty = SimpleStringProperty(tag.name)
    val value: DoubleProperty = SimpleDoubleProperty(tag.value ?: 0.0)

    fun selectedProperty(): BooleanProperty = selected
    fun nameProperty(): StringProperty = name
    fun valueProperty(): DoubleProperty = value

    fun getName(): String = name.get()
    fun getValue(): Double = value.get()

    fun setSelected(selected: Boolean) {
        this.selected.set(selected)
    }
}