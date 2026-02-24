package com.FrameHopper.app.ui.controller;

import com.FrameHopper.app.View.Elements.FXElementsProviders.FXIconLoader;
import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.boundry.dto.TagDTO;
import com.FrameHopper.app.core.ports.in.tag.DeleteTagCommand;
import com.FrameHopper.app.core.ports.in.tag.TagsQuery;
import com.FrameHopper.app.ui.FXMLViewLoader;
import com.FrameHopper.app.ui.UiView;
import com.FrameHopper.app.ui.eventing.TagUpdatedEventDispatcher;
import com.FrameHopper.app.ui.eventing.TagUpdatedEventListener;
import com.FrameHopper.app.ui.ve.TagManagerTableEntry;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import lombok.NonNull;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Scope("prototype")
public class TagManagerController extends UiView implements TagUpdatedEventListener {
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

    public TagManagerController(TagsQuery tagsQuery, DeleteTagCommand deleteTagCommand) {
        this.tagsQuery = tagsQuery;
        this.deleteTagCommand = deleteTagCommand;

        TagUpdatedEventDispatcher.register(this);
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
                e -> {
                    var loader = FXMLViewLoader.getView(
                            "TagDetailsViewModel",
                            "Create Tag",
                            tagManagerView
                    );

                    TagDetailsController controller = loader.getController();
                    controller.init(e.getTag());
                }
        );

        setFactoryForButtonCell(
                deleteColumn,
                "bin.png",
                e -> {
                    deleteTagCommand.DeleteTag(e.getTag().getId());
                    TagUpdatedEventDispatcher.dispatchDelete(e.getTag());
                });

        loadTagTable();

        addCodeButton.setText(Dictionary.get("settings.button.add"));
        addCodesButton.setText(Dictionary.get("settings.button.add.multi"));
        hideCodesButton.setText(Dictionary.get("settings.button.hide"));
        unhideCodesButton.setText(Dictionary.get("settings.button.unhide"));
        deleteCodesButton.setText(Dictionary.get("settings.button.delete"));

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
    protected void loadTags() {
    }

    @FXML
    protected void hideTags() {
    }

    @FXML
    protected void unhideTags() {
    }

    @FXML
    protected void deleteTags() {
    }

    //endregion

    //region [TAG LIST UPDATE]

    @Override
    public void onTagUpdated(TagDTO tagDTO) {
        loadTagTable();
    }

    @Override
    public void onTagCreated(TagDTO tagDTO) {
        loadTagTable();
    }

    @Override
    public void onTagDeleted(TagDTO tagDTO) {
        loadTagTable();
    }

    //endregion

    @Override
    public void addKeybinds() {

    }

    @Override
    public void close() {
        TagUpdatedEventDispatcher.unregister(this);

        var stage = (Stage) tagManagerView.getScene().getWindow();
        stage.close();
    }
}
