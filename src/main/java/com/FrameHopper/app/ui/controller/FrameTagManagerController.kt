package com.FrameHopper.app.ui.controller

import com.FrameHopper.app.boundry.dto.FrameDTO
import com.FrameHopper.app.boundry.dto.TagDTO
import com.FrameHopper.app.boundry.dto.VideoDTO
import com.FrameHopper.app.core.ports.`in`.frame.CreateFrameCommand
import com.FrameHopper.app.core.ports.`in`.frame.DeleteFrameCommand
import com.FrameHopper.app.core.ports.`in`.frame.UpdateFrameCommand
import com.FrameHopper.app.core.ports.`in`.tag.TagsQuery
import com.FrameHopper.app.core.ports.out.UserSettingsPort
import com.FrameHopper.app.ui.UIFlag
import com.FrameHopper.app.ui.UIManager
import com.FrameHopper.app.ui.UiView
import com.FrameHopper.app.ui.actions.HistoryActions
import com.FrameHopper.app.ui.actions.PasteRecentAction
import com.FrameHopper.app.ui.actions.RemoveRecentAction
import com.FrameHopper.app.ui.eventing.*
import com.FrameHopper.app.ui.utils.SearchUtils
import javafx.application.Platform
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
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
import java.util.function.Predicate

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class FrameTagManagerController (
    private val tagsQuery: TagsQuery,
    private val createFrameCommand: CreateFrameCommand,
    private val updateFrameCommand: UpdateFrameCommand,
    private val deleteFrameCommand: DeleteFrameCommand,
    private val uiManager: UIManager,
    private val userSettings: UserSettingsPort,
    private val pasteRecentAction: PasteRecentAction,
    private val removeRecentAction: RemoveRecentAction,
    private val historyActions: HistoryActions
) : UiView(),
    TagDeletedEventListener,
    TagCreatedEventListener,
    TagUpdatedEventListener,
    FrameUpdatedEventListener,
    VideoDeletedEventListener,
    ShowHiddenEventListener
{
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
    private val cachedTagList: ObservableList<FrameTagManagerTableEntry?> = FXCollections.observableArrayList()
    private lateinit var filteredCache: FilteredList<FrameTagManagerTableEntry?>

    private val addedCache = mutableListOf<TagDTO>()
    private val removedCache = mutableListOf<TagDTO>()

    init {
        TagDeletedEventDispatcher.register(this)
        TagUpdatedEventDispatcher.register(this)
        TagCreatedEventDispatcher.register(this)
        FrameUpdatedEventDispatcher.register(this)
        VideoDeletedEventDispatcher.register(this)
        ShowHiddenEventDispatcher.register(this)
    }

    @FXML
    fun initialize() {
        frameTagManagerView.setOnMouseClicked {_ ->
            frameTagManagerView.requestFocus()
            codeTable.selectionModel.clearSelection()
        }

        codeColumn.cellValueFactory = PropertyValueFactory("name")
        bind(codeColumn, "ftm.table.name")

        valueColumn.cellValueFactory = PropertyValueFactory("value")
        bind(valueColumn, "ftm.table.value")

        selectColumn.cellValueFactory = Callback {it.value?.selected}
        selectColumn.cellFactory = CheckBoxTableCell.forTableColumn(selectColumn)

        codeTable.stylesheets.add(
            javaClass.classLoader?.getResource("styling/tag-table.css")?.toExternalForm()
        )
        codeTable.setRowFactory { _: TableView<FrameTagManagerTableEntry?> ->
            object : TableRow<FrameTagManagerTableEntry?>() {
                override fun updateItem(item: FrameTagManagerTableEntry?, empty: Boolean) {
                    super.updateItem(item, empty)

                    styleClass.remove("hidden-tag-row")

                    if (empty || item == null) {
                        return
                    }

                    if (!item.cachedTag.visible) {
                        styleClass.add("hidden-tag-row")
                    }
                }
            }
        }

        loadTagTable()

        bind(cancelButton, "ftm.button.cancel")
        cancelButton.setOnAction { _: ActionEvent? -> close() }

        bind(saveButton, "ftm.button.save")
        bind(searchField, "ftm.search-prompt")

        addKeybinds()

        Platform.runLater {
            val stage = frameTagManagerView.scene.window as Stage
            stage.onCloseRequest = EventHandler { _: WindowEvent? -> close() }
            frameTagManagerView.requestFocus()
        }
    }

    fun init(frame: FrameDTO) {
        bind(frameLabel, "ftm.frame-info",frame.frameNumber + 1)

        val tags = frame.tags

        if(!tags.isNullOrEmpty())
            tags.forEach { t ->
                codeTable.items
                    .firstOrNull { it?.cachedTag?.id == t.id }
                    ?.setSelected(true)
            }

        cachedFrame = frame
    }

    fun loadTagTable(){
        cachedTagList.addAll(tagsQuery.getAllTags()
            ?.takeIf { it.isNotEmpty() }
            ?.map { tag -> FrameTagManagerTableEntry(tag).apply {
                selected.addListener { _, _, newValue ->
                    if(!::cachedFrame.isInitialized) return@addListener

                    if(newValue == true) addedCache.add(tag)
                    else removedCache.add(tag)
                }
            }} ?: emptyList())

        filteredCache = FilteredList<FrameTagManagerTableEntry?>(cachedTagList);
        codeTable.items = filteredCache

        refreshVisibilityFilter()
    }

    fun refreshVisibilityFilter() {
        filteredCache.predicate = Predicate { e: FrameTagManagerTableEntry? ->
            e != null &&
                    (userSettings.showHidden() || e.cachedTag.visible)
        }

        codeTable.refresh()
    }

    @FXML
    fun handleSearch() {
        SearchUtils.handleSearch(
            searchButton,
            searchField,
            codeTable,
            cachedTagList
        ) { list, query ->
            list.filtered { it?.cachedTag?.name?.contains(query, true) == true }
        }
    }

    @FXML
    fun save() {
        pasteRecentAction.addTags(addedCache)
        removeRecentAction.addTags(removedCache)

        val selected = codeTable.items
            ?.asSequence()
            ?.filter { it!!.selected.value}
            ?.map { it!!.cachedTag }
            ?.toList()

        if(selected.isNullOrEmpty()) {
            if(cachedFrame.id != -1) {
                historyActions.new(cachedFrame.frameNumber, cachedFrame)
                deleteFrameCommand.deleteFrame(cachedFrame.id)
                FrameUpdatedEventDispatcher.dispatch(cachedFrame.frameNumber, null)
            }

            close()
            return
        }

        if(selected.isEmpty()) historyActions.new(cachedFrame.frameNumber, null)
        else historyActions.new(cachedFrame.frameNumber, cachedFrame)

        cachedFrame.tags.apply {
            clear()
            addAll(selected)
        }

        cachedFrame =
            if(cachedFrame.id == -1) createFrameCommand.createFrame(cachedFrame)
            else updateFrameCommand.updateFrame(cachedFrame)

        FrameUpdatedEventDispatcher.dispatch(cachedFrame.frameNumber, cachedFrame)
        close()
    }

    override fun addKeybinds() {
        keyActions[KeyCodeCombination(KeyCode.C, KeyCombination.SHIFT_DOWN)] = Runnable { close() }

        addEventFilter(frameTagManagerView)
    }

    @FXML
    override fun close() {
        TagDeletedEventDispatcher.unregister (this)
        TagUpdatedEventDispatcher.unregister (this)
        TagCreatedEventDispatcher.unregister (this)
        FrameUpdatedEventDispatcher.unregister (this)
        VideoDeletedEventDispatcher.unregister (this)
        ShowHiddenEventDispatcher.unregister (this)

        addedCache.clear()
        removedCache.clear()

        uiManager.close(UIFlag.FRAME_TAG_MANAGER)
        val stage = frameTagManagerView.scene.window as Stage
        stage.close()
    }

    //region [LISTENERS]

    override fun onTagDeleted(tag: TagDTO) {
        val tag = cachedTagList.find { it?.cachedTag == tag }
        cachedTagList.remove(tag)
    }

    override fun onTagDeleted(tags: List<TagDTO>) {
        val tags = cachedTagList.filter { tags.contains(it?.cachedTag) }
        cachedTagList.removeAll(tags)
    }

    override fun onTagCreated(tag: TagDTO) {
        cachedTagList.add(FrameTagManagerTableEntry(tag).apply {
            selected.addListener { _, _, newValue ->
                if(newValue == true) addedCache.add(tag)
                else removedCache.add(tag)
            }
        })
    }

    override fun onTagCreated(tags: List<TagDTO>) {
        cachedTagList.addAll(tags.map {
            FrameTagManagerTableEntry(it).apply {
                selected.addListener { _, _, newValue ->
                    if (newValue == true) addedCache.add(cachedTag)
                    else removedCache.add(cachedTag)
                }
            }
        })
    }

    override fun onTagUpdated(tag: TagDTO) {
        cachedTagList.find { it?.cachedTag == tag }?.setTag(tag)
        refreshVisibilityFilter()
    }

    override fun onTagUpdated(tags: List<TagDTO>) {
        val tagMap = tags.associateBy { it }
        cachedTagList.forEach{ e ->
            val updated = tagMap[e?.cachedTag] ?: return@forEach
            e?.setTag(updated)
        }
        refreshVisibilityFilter()
    }

    override fun onFrameUpdate(frameNumber: Int, frame: FrameDTO?) {
        if(frameNumber != cachedFrame.frameNumber) return

        cachedFrame = frame ?: FrameDTO(-1, frameNumber, cachedFrame.video, ArrayList())

        val tagSet = cachedFrame.tags.toSet()

        cachedTagList.forEach { e ->
            e?.setSelected(e.cachedTag in tagSet)
        }

        addedCache.clear()
        removedCache.clear()
    }

    override fun onDeleteVideo(video: VideoDTO) {
        if(video == cachedFrame.video()) close()
    }

    override fun onSHowHiddenUpdated() {
        refreshVisibilityFilter()
    }

    //endregion
}

class FrameTagManagerTableEntry(var cachedTag: TagDTO) {

    val selected: BooleanProperty = SimpleBooleanProperty(false)
    val name: StringProperty = SimpleStringProperty(cachedTag.name)
    val value: DoubleProperty = SimpleDoubleProperty(cachedTag.value ?: 0.0)

    fun selectedProperty(): BooleanProperty = selected
    fun nameProperty(): StringProperty = name
    fun valueProperty(): DoubleProperty = value

    fun getName(): String = name.get()
    fun getValue(): Double = value.get()

    fun setSelected(selected: Boolean) = this.selected.set(selected)

    fun setTag(tag: TagDTO) {
        this.cachedTag = tag
        name.set(tag.name)
        value.set(tag.value ?: -1.0)
    }
}