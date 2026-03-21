package com.FrameHopper.app.ui.controller

import com.FrameHopper.app.boundry.dto.FrameDTO
import com.FrameHopper.app.boundry.dto.TagDTO
import com.FrameHopper.app.core.ports.`in`.frame.CreateFrameCommand
import com.FrameHopper.app.core.ports.`in`.frame.DeleteFrameCommand
import com.FrameHopper.app.core.ports.`in`.frame.UpdateFrameCommand
import com.FrameHopper.app.core.ports.`in`.tag.TagsQuery
import com.FrameHopper.app.ui.UIFlag
import com.FrameHopper.app.ui.UIManager
import com.FrameHopper.app.ui.UiView
import com.FrameHopper.app.ui.eventing.FrameUpdatedEventDispatcher
import javafx.application.Platform
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.control.cell.CheckBoxTableCell
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
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
    private val deleteFrameCommand: DeleteFrameCommand,
    private val uiManager: UIManager
) : UiView() {
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
    private lateinit var cachedTagList: ObservableList<FrameTagManagerTableEntry?>

    @FXML
    fun initialize() {
        codeColumn.cellValueFactory = PropertyValueFactory("name")
        bind(codeColumn, "ftm.table.name")

        valueColumn.cellValueFactory = PropertyValueFactory("value")
        bind(valueColumn, "ftm.table.value")

        selectColumn.cellValueFactory = Callback {it.value?.selected}
        selectColumn.cellFactory = CheckBoxTableCell.forTableColumn(selectColumn)

        cachedTagList = FXCollections.observableArrayList(
            tagsQuery.getAllTags()?.takeIf { it.isNotEmpty() }
                ?.map(::FrameTagManagerTableEntry)
        )
        codeTable.items = cachedTagList

        bind(cancelButton, "ftm.button.cancel")
        cancelButton.setOnAction { _: ActionEvent? -> close() }

        bind(saveButton, "ftm.button.save")
        bind(searchField, "ftm.search-prompt")

        addKeybinds()

        Platform.runLater {
            val stage = frameTagManagerView.scene.window as Stage
            bind(stage, "ftm.stage")
            stage.onCloseRequest = EventHandler { _: WindowEvent? -> close() }
            frameTagManagerView.requestFocus()
        }
    }

    fun init(frame: FrameDTO) {
        bind(frameLabel, "ftm.frame-info",frame.frameNumber + 1)
        cachedFrame = frame

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
            codeTable.items = FXCollections.observableArrayList(cachedTagList.filter { it?.tag?.name?.contains(searchField.text, true) == true })
            searchButton.text = "X"
        } else {
            codeTable.items = cachedTagList
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

    override fun addKeybinds() {
        keyActions[KeyCodeCombination(KeyCode.C, KeyCombination.SHIFT_DOWN)] = Runnable { close() }

        addEventFilter(frameTagManagerView)
    }

    @FXML
    override fun close() {
        uiManager.close(UIFlag.FRAME_TAG_MANAGER)
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