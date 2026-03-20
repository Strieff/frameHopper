package com.FrameHopper.app.ui.controller;

import com.FrameHopper.app.View.Elements.FXElementsProviders.FXIconLoader;
import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.boundry.dto.TagDTO;
import com.FrameHopper.app.core.ports.in.tag.ChangeTagStatusCommand;
import com.FrameHopper.app.core.ports.in.tag.CreateTagCommand;
import com.FrameHopper.app.core.ports.in.tag.DeleteTagCommand;
import com.FrameHopper.app.core.ports.in.tag.TagsQuery;
import com.FrameHopper.app.ui.FXMLViewLoader;
import com.FrameHopper.app.ui.UIFlag;
import com.FrameHopper.app.ui.UIManager;
import com.FrameHopper.app.ui.UiView;
import com.FrameHopper.app.ui.dialog.FileChooserProvider;
import com.FrameHopper.app.ui.eventing.*;
import com.FrameHopper.app.ui.ve.TagManagerTableEntry;
import javafx.application.Platform;
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
        TagDeletedEventListener
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

    public TagManagerController(
            TagsQuery tagsQuery,
            DeleteTagCommand deleteTagCommand,
            CreateTagCommand createTagCommand,
            ChangeTagStatusCommand changeTagStatusCommand,
            UIManager uiManager
    ) {
        this.tagsQuery = tagsQuery;
        this.deleteTagCommand = deleteTagCommand;
        this.createTagCommand = createTagCommand;
        this.changeTagStatusCommand = changeTagStatusCommand;
        this.uiManager = uiManager;

        TagCreatedEventDispatcher.register(this);
        TagUpdatedEventDispatcher.register(this);
        TagDeletedEventDispatcher.register(this);
    }

    @FXML
    private void initialize() {
        codeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        codeColumn.setText(Dictionary.get("name"));
        setFactoryForTextCell(codeColumn);

        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueColumn.setText(Dictionary.get("value"));

        descriptionColumn.setText(Dictionary.get("description"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        setFactoryForTextCell(descriptionColumn);

        codeTable.setPlaceholder(new Label(Dictionary.get("placeholder.codes")));

        setFactoryForButtonCell(
                editColumn,
                "edit.png",
                e -> openTagDetails(e.getTag())
        );

        setFactoryForButtonCell(
                deleteColumn,
                "bin.png",
                e -> {
                    deleteTagCommand.DeleteTag(e.getTag().getId());
                    TagDeletedEventDispatcher.dispatchDelete(e.getTag());
                });

        loadTagTable();

        addCodeButton.setText(Dictionary.get("settings.button.add"));
        addCodesButton.setText(Dictionary.get("settings.button.add.multi"));
        hideCodesButton.setText(Dictionary.get("settings.button.hide"));
        unhideCodesButton.setText(Dictionary.get("settings.button.unhide"));
        deleteCodesButton.setText(Dictionary.get("settings.button.delete"));

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
        codeTable.getItems().clear();
        codeTable.getItems().addAll(tagsQuery.getAllTags().stream().map(TagManagerTableEntry::new).toList());
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
                .map(TagManagerTableEntry::getTag)
                .filter(TagDTO::getVisible)
                .toList();
        var selectedIds = selected.stream().map(TagDTO::getId).toList();

        changeTagStatusCommand.ChangeTagStatus(selectedIds);

        selected.forEach(t -> t.setVisible(false));
        TagUpdatedEventDispatcher.dispatchUpdate(selected);
    }

    @FXML
    protected void unhideTags() {
        var selectedItems = getSelected();
        if(selectedItems == null || selectedItems.isEmpty()) return;

        var selected = selectedItems.stream()
                .map(TagManagerTableEntry::getTag)
                .filter(t -> !t.getVisible())
                .toList();
        var selectedIds = selected.stream().map(TagDTO::getId).toList();

        changeTagStatusCommand.ChangeTagStatus(selectedIds);
        selected.forEach(t -> t.setVisible(true));

        TagUpdatedEventDispatcher.dispatchUpdate(selected);
    }

    @FXML
    protected void deleteTags() {
        var selectedItems = getSelected();
        if(selectedItems == null || selectedItems.isEmpty()) return;

        var selected = selectedItems.stream()
                .map(TagManagerTableEntry::getTag)
                .toList();
        var selectedIds = selected.stream().map(TagDTO::getId).toList();

        deleteTagCommand.DeleteTags(selectedIds);
        TagDeletedEventDispatcher.dispatchDelete(selected);
    }

    private List<TagManagerTableEntry> getSelected() {
        return codeTable.getSelectionModel().getSelectedItems();
    }

    //endregion

    //region [TAG LIST UPDATE]

    @Override
    public void onTagCreated(@NotNull TagDTO tagDTO) {
        codeTable.getItems().add(new TagManagerTableEntry(tagDTO));
    }

    @Override
    public void onTagCreated(@NotNull List<TagDTO> tags) {
        codeTable.getItems().addAll(tags.stream().map(TagManagerTableEntry::new).toList());
    }

    @Override
    public void onTagUpdated(@NotNull TagDTO tagDTO) {
        var entry = codeTable.getItems().stream()
                .filter(e ->  e.getTag().equals(tagDTO))
                .findFirst().orElse(null);

        if(entry == null) return;

        entry.setTag(tagDTO);
        codeTable.refresh();
    }

    @Override
    public void onTagUpdated(@NotNull List<TagDTO> tags) {
        var entries = codeTable.getItems().stream()
                .filter(e -> tags.contains(e.getTag()))
                .toList();

        if(entries.isEmpty()) return;

        codeTable.getItems().forEach(e -> {
            var tag = tags.stream().filter(t -> e.getTag().equals(t)).findFirst().orElse(null);

            if(tag ==null) return;

            e.setTag(tag);
        });
    }

    @Override
    public void onTagDeleted(@NotNull TagDTO tagDTO) {
        var entry = codeTable.getItems().stream()
                .filter(e ->  e.getTag().equals(tagDTO))
                .findFirst().orElse(null);

        if(entry == null) return;

        codeTable.getItems().remove(entry);
    }

    @Override
    public void onTagDeleted(@NotNull List<TagDTO> tags) {
        var entries = codeTable.getItems().stream()
                .filter(e -> tags.contains(e.getTag()))
                .toList();

        if(entries.isEmpty()) return;

        codeTable.getItems().removeAll(entries);
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

        var stage = (Stage) tagManagerView.getScene().getWindow();
        stage.close();
    }
}
