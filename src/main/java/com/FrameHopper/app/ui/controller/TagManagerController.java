package com.FrameHopper.app.ui.controller;

import com.FrameHopper.app.ui.utils.FXIconLoader;
import com.FrameHopper.app.ui.settings.UserSettingsAdapter;
import com.FrameHopper.app.boundry.dto.TagDTO;
import com.FrameHopper.app.core.ports.in.tag.ChangeTagStatusCommand;
import com.FrameHopper.app.core.ports.in.tag.CreateTagCommand;
import com.FrameHopper.app.core.ports.in.tag.DeleteTagCommand;
import com.FrameHopper.app.core.ports.in.tag.TagsQuery;
import com.FrameHopper.app.ui.settings.UserSettingsPort;
import com.FrameHopper.app.ui.FXMLViewLoader;
import com.FrameHopper.app.ui.UIFlag;
import com.FrameHopper.app.ui.UIManager;
import com.FrameHopper.app.ui.UiView;
import com.FrameHopper.app.ui.dialog.FileChooserProvider;
import com.FrameHopper.app.ui.eventing.*;
import com.FrameHopper.app.ui.ve.TagManagerTableEntry;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

@Component
@Scope("prototype")
public class TagManagerController extends UiView implements
        TagCreatedEventListener,
        TagUpdatedEventListener,
        TagDeletedEventListener,
        ShowHiddenEventListener
{
    @FXML
    private TableView<TagManagerTableEntry> codeTable;
    @FXML
    private TableColumn<TagManagerTableEntry, String> codeColumn;
    @FXML
    private TableColumn<TagManagerTableEntry, Double> valueColumn;
    @FXML
    private TableColumn<TagManagerTableEntry, String> descriptionColumn;
    @FXML
    private TableColumn<TagManagerTableEntry, Void> editColumn, deleteColumn;
    @FXML
    private BorderPane tagManagerView;
    @FXML
    private Button
            addCodeButton,
            addCodesButton,
            hideCodesButton,
            unhideCodesButton,
            deleteCodesButton;

    private final TagsQuery tagsQuery;
    private final DeleteTagCommand deleteTagCommand;
    private final CreateTagCommand createTagCommand;
    private final ChangeTagStatusCommand changeTagStatusCommand;
    private final UIManager uiManager;
    private final UserSettingsPort userSettings;

    private final ObservableList<TagManagerTableEntry> cachedTags = FXCollections.observableArrayList();
    private FilteredList<TagManagerTableEntry> filteredCache;

    public TagManagerController(
            TagsQuery tagsQuery,
            DeleteTagCommand deleteTagCommand,
            CreateTagCommand createTagCommand,
            ChangeTagStatusCommand changeTagStatusCommand,
            UIManager uiManager,
            UserSettingsAdapter userSettings
    ) {
        this.tagsQuery = tagsQuery;
        this.deleteTagCommand = deleteTagCommand;
        this.createTagCommand = createTagCommand;
        this.changeTagStatusCommand = changeTagStatusCommand;
        this.uiManager = uiManager;
        this.userSettings = userSettings;

        TagCreatedEventDispatcher.register(this);
        TagUpdatedEventDispatcher.register(this);
        TagDeletedEventDispatcher.register(this);
        ShowHiddenEventDispatcher.register(this);
    }

    @FXML
    private void initialize() {
        tagManagerView.setOnMouseClicked(e -> codeTable.getSelectionModel().clearSelection());

        codeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        bind(codeColumn, "tm.table.name");
        setFactoryForTextCell(codeColumn);

        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        bind(valueColumn, "tm.table.value");

        bind(descriptionColumn, "tm.table.description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        setFactoryForTextCell(descriptionColumn);

        setFactoryForButtonCell(
                editColumn,
                "edit.png",
                e -> openTagDetails(e.getTag())
        );

        setFactoryForButtonCell(
                deleteColumn,
                "bin.png",
                e -> {
                    deleteTagCommand.deleteTag(e.getTag().getId());
                    TagDeletedEventDispatcher.dispatchDelete(e.getTag());
                });

        codeTable.getStylesheets().add(
                getClass().getClassLoader().getResource("styling/tag-table.css").toExternalForm()
        );
        codeTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(TagManagerTableEntry item, boolean empty) {
                super.updateItem(item, empty);

                getStyleClass().remove("hidden-tag-row");

                if (empty || item == null) {
                    return;
                }

                if (!item.getTag().getVisible()) {
                    getStyleClass().add("hidden-tag-row");
                }
            }
        });

        loadTagTable();

        bind(addCodeButton, "tm.button.add-tag");
        bind(addCodesButton, "tm.button.add-tags");
        bind(hideCodesButton, "tm.button.hide-tags");
        bind(unhideCodesButton, "tm.button.unhide-tags");
        bind(deleteCodesButton, "tm.button.delete-tags");

        addKeybinds();

        Platform.runLater(() -> {
            var stage = (Stage) tagManagerView.getScene().getWindow();
            stage.setOnCloseRequest(e -> close());
        });
    }

    //region Helpers

    private void setFactoryForTextCell(@NonNull TableColumn<TagManagerTableEntry, String> column) {
        column.setCellFactory(c -> new TableCell<>() {
            private final Text text = new Text();

            {
                text.wrappingWidthProperty().bind(c.widthProperty().subtract(10)); // padding
                setGraphic(text);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                text.setText(empty || item == null ? null : item);
            }
        });
    }

    private void setFactoryForButtonCell(@NonNull TableColumn<TagManagerTableEntry, Void> column, String iconPath, Consumer<TagManagerTableEntry> action) {
        column.setCellFactory(new Callback<>() {
            @Override
            public TableCell<TagManagerTableEntry, Void> call(final TableColumn<TagManagerTableEntry, Void> param) {
                return new TableCell<>() {
                    private final Button button = new Button();
                    private final HBox centeredBox = new HBox(button);

                    {
                        button.setGraphic(new ImageView(FXIconLoader.getSmallIcon(iconPath)));
                        centeredBox.setAlignment(Pos.CENTER);

                        button.setOnAction(e -> {
                            var rowItem = getTableRow().getItem();

                            if (rowItem != null)
                                action.accept(rowItem);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(centeredBox);
                        }
                    }
                };
            }
        });
    }

    private void loadTagTable(){
        cachedTags.addAll(tagsQuery.getAllTags().stream().map(TagManagerTableEntry::new).toList());

        filteredCache = new FilteredList<>(cachedTags);
        codeTable.setItems(filteredCache);

        refreshVisibilityFilter();
    }

    private void refreshVisibilityFilter() {
        filteredCache.setPredicate(e ->
                e != null &&
                (userSettings.showHidden() || e.getTag().getVisible())
        );

        codeTable.refresh();
    }

    //endregion

    private void openTagDetails(TagDTO tag){
        var loader = uiManager.open(UIFlag.TAG_DETAILS, tagManagerView);

        TagDetailsController controller = loader.getController();
        controller.init(tag);
    }

    private void openTagDetails(){
        var selectedItems = codeTable.getSelectionModel().getSelectedItems();
        if(selectedItems == null || selectedItems.isEmpty()) return;

        var selected = selectedItems.stream().map(TagManagerTableEntry::getTag);

        selected.forEach(this::openTagDetails);
    }

    //region JFX

    @FXML
    protected void openTagCreation() {
        var loader = FXMLViewLoader.getView(
                "TagDetailsViewModel",
                "Create Tag",
                tagManagerView
        );

        TagDetailsController controller = loader.getController();
        controller.init();
    }

    @FXML
    protected void importTags() {
        try {
            var path = FileChooserProvider.textFileChooser((Stage) tagManagerView.getScene().getWindow());
            var lines = Files.readAllLines(Path.of(path));
            var tags = lines.stream()
                    .map(line -> {
                        var data = line.split(";");
                        return new TagDTO(data[0], Double.parseDouble(data[1]), data.length == 2 ? "" : data[2]);
                    })
                    .toList();

            tags = createTagCommand.CreateTags(tags);
            TagCreatedEventDispatcher.dispatchCreate(tags);
        } catch (Exception e) {
            //TODO
            e.printStackTrace();
        }
    }

    @FXML
    protected void hideTags() {
        var selectedItems = getSelected();
        if(selectedItems == null || selectedItems.isEmpty()) return;

        var selected = selectedItems.stream()
                .filter(e -> e.getTag().getVisible())
                .toList();
        var selectedIds = selected.stream().map(e -> e.getTag().getId()).toList();

        changeTagStatusCommand.ChangeTagStatus(selectedIds);

        selected.forEach(t -> t.updateVisibility(false));
        TagUpdatedEventDispatcher.dispatchUpdate(selected.stream().map(TagManagerTableEntry::getTag).toList());
        refreshVisibilityFilter();
    }

    @FXML
    protected void unhideTags() {
        var selectedItems = getSelected();
        if(selectedItems == null || selectedItems.isEmpty()) return;

        var selected = selectedItems.stream()
                .filter(e -> !e.getTag().getVisible())
                .toList();
        var selectedIds = selected.stream().map(e -> e.getTag().getId()).toList();

        changeTagStatusCommand.ChangeTagStatus(selectedIds);
        selected.forEach(t -> t.getTag().setVisible(true));

        TagUpdatedEventDispatcher.dispatchUpdate(selected.stream().map(TagManagerTableEntry::getTag).toList());
        refreshVisibilityFilter();
    }

    @FXML
    protected void deleteTags() {
        var selectedItems = getSelected();
        if(selectedItems == null || selectedItems.isEmpty()) return;

        var selected = selectedItems.stream()
                .map(TagManagerTableEntry::getTag)
                .toList();
        var selectedIds = selected.stream().map(TagDTO::getId).toList();

        deleteTagCommand.deleteTags(selectedIds);
        TagDeletedEventDispatcher.dispatchDelete(selected);
    }

    private List<TagManagerTableEntry> getSelected() {
        return codeTable.getSelectionModel().getSelectedItems();
    }

    //endregion

    //region [TAG LIST UPDATE]

    @Override
    public void onTagCreated(@NotNull TagDTO tagDTO) {
        cachedTags.add(new TagManagerTableEntry(tagDTO));
        refreshVisibilityFilter();
    }

    @Override
    public void onTagCreated(@NotNull List<TagDTO> tags) {
        cachedTags.addAll(tags.stream().map(TagManagerTableEntry::new).toList());
        refreshVisibilityFilter();
    }

    @Override
    public void onTagUpdated(@NotNull TagDTO tagDTO) {
        var entry = cachedTags.stream()
                .filter(e ->  e.getTag().equals(tagDTO))
                .findFirst().orElse(null);

        if(entry == null) return;

        entry.setTag(tagDTO);
        refreshVisibilityFilter();
    }

    @Override
    public void onTagUpdated(@NotNull List<TagDTO> tags) {
        var entries = cachedTags.stream()
                .filter(e -> tags.contains(e.getTag()))
                .toList();

        if(entries.isEmpty()) return;

        cachedTags.forEach(e -> {
            var tag = tags.stream().filter(t -> e.getTag().equals(t)).findFirst().orElse(null);

            if(tag ==null) return;

            e.setTag(tag);
        });

        refreshVisibilityFilter();
    }

    @Override
    public void onTagDeleted(@NotNull TagDTO tagDTO) {
        cachedTags.removeIf(e -> e.getTag().equals(tagDTO));
        refreshVisibilityFilter();
    }

    @Override
    public void onTagDeleted(@NotNull List<TagDTO> tags) {
        cachedTags.removeIf(e -> tags.contains(e.getTag()));
        refreshVisibilityFilter();
    }

    //endregion

    @Override
    public void addKeybinds() {
        keyActions.put(new KeyCodeCombination(KeyCode.C, KeyCombination.SHIFT_DOWN), this::close);

        keyActions.put(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN), this::openTagDetails);
        keyActions.put(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN), this::openTagCreation);
        keyActions.put(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN), this::importTags);
        keyActions.put(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN), this::hideTags);
        keyActions.put(new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN), this::unhideTags);
        keyActions.put(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN), this::deleteTags);

        addEventFilter(tagManagerView);
    }

    @Override
    public void close() {
        uiManager.close(UIFlag.TAG_MANAGER);
        TagCreatedEventDispatcher.unregister(this);
        TagUpdatedEventDispatcher.unregister(this);
        TagDeletedEventDispatcher.unregister(this);
        ShowHiddenEventDispatcher.unregister(this);

        var stage = (Stage) tagManagerView.getScene().getWindow();
        stage.close();
    }

    @Override
    public void onShowHiddenUpdated() {
        refreshVisibilityFilter();
    }
}
