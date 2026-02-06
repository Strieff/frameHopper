package com.FrameHopper.app.ui.controller;

import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.boundry.dto.FrameDTO;
import com.FrameHopper.app.core.domain.Frame;
import com.FrameHopper.app.core.ports.in.frame.CreateFrameCommand;
import com.FrameHopper.app.core.ports.in.frame.DeleteFrameCommand;
import com.FrameHopper.app.core.ports.in.frame.UpdateFrameCommand;
import com.FrameHopper.app.core.ports.in.tag.TagsQuery;
import com.FrameHopper.app.ui.UiView;
import com.FrameHopper.app.ui.eventing.FrameUpdatedEventDispatcher;
import com.FrameHopper.app.ui.eventing.FrameUpdatedListener;
import com.FrameHopper.app.ui.ve.FrameTagManagerTableEntry;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class FrameTagManagerController implements UiView {
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton, cancelButton, saveButton;
    @FXML
    private TableView<FrameTagManagerTableEntry> codeTable;
    @FXML
    private TableColumn<FrameTagManagerTableEntry, Boolean> selectColumn;
    @FXML
    private TableColumn<FrameTagManagerTableEntry, String> codeColumn;
    @FXML
    private TableColumn<FrameTagManagerTableEntry, Double> valueColumn;
    @FXML
    private Label frameLabel;
    @FXML
    private BorderPane frameTagManagerView;

    private final TagsQuery tagsQuery;
    private final CreateFrameCommand createFrameCommand;
    private final UpdateFrameCommand updateFrameCommand;
    private final DeleteFrameCommand deleteFrameCommand;

    private FrameDTO cachedFrame;

    public FrameTagManagerController(
            TagsQuery tagsQuery,
            CreateFrameCommand createFrameCommand,
            UpdateFrameCommand updateFrameCommand,
            DeleteFrameCommand deleteFrameCommand
    ) {
        this.tagsQuery = tagsQuery;
        this.createFrameCommand = createFrameCommand;
        this.updateFrameCommand = updateFrameCommand;
        this.deleteFrameCommand = deleteFrameCommand;
    }

    @FXML
    private void initialize() {
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        codeColumn.setText(Dictionary.get("name"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueColumn.setText(Dictionary.get("value"));
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn((Integer index) -> codeTable.getItems().get(index).selectedProperty()));

        var tags = tagsQuery.getAllTags();

        if(tags != null && !tags.isEmpty())
            codeTable.getItems().addAll(tags.stream().map(FrameTagManagerTableEntry::new).toList());

        cancelButton.setText(Dictionary.get("cancel"));
        cancelButton.setOnAction(event -> close());

        Platform.runLater(() -> {
            var stage = (Stage) frameTagManagerView.getScene().getWindow();
            stage.setOnCloseRequest(e -> close());
            frameTagManagerView.requestFocus();
        });
    }

    public void init(FrameDTO frame) {
        cachedFrame = frame;

        frameLabel.setText(String.format(
                Dictionary.get("tm.frame"),
                (cachedFrame.frameNumber() + 1)
        ));

        var tags = cachedFrame.tags();
        if(tags == null || tags.isEmpty()) return;

        tags.forEach(t -> {
            var tableTags = codeTable.getItems().stream().filter(te -> te.getTag().getId() == t.getId()).toList();
            if(tableTags.isEmpty()) return;

            tableTags.getFirst().setSelected(true);
        });
    }

    @FXML
    public void save() {
        var selected = codeTable.getItems().stream().
                filter(e -> e.getSelected().getValue())
                .map(FrameTagManagerTableEntry::getTag)
                .toList();

        if(cachedFrame.id() == -1 && selected.isEmpty()) {
            close();
            return;
        }

        if(cachedFrame.id() != -1 && selected.isEmpty()) {
            deleteFrameCommand.deleteFrame(cachedFrame.id());

            FrameUpdatedEventDispatcher.dispatch(cachedFrame.frameNumber(), null);

            close();
            return;
        }

        cachedFrame.tags().clear();
        cachedFrame.tags().addAll(selected);

        if(cachedFrame.id() == -1)
            cachedFrame = createFrameCommand.createFrame(cachedFrame);
        else
            cachedFrame = updateFrameCommand.updateFrame(cachedFrame);

        FrameUpdatedEventDispatcher.dispatch(cachedFrame.frameNumber(), cachedFrame);
        close();
    }

    @FXML
    @Override
    public void close() {
        var stage = (Stage) frameTagManagerView.getScene().getWindow();
        stage.close();
    }

}
