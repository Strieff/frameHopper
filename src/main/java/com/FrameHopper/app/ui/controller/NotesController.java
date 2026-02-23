package com.FrameHopper.app.ui.controller;

import com.FrameHopper.app.View.Elements.FXElementsProviders.FXIconLoader;
import com.FrameHopper.app.boundry.dto.CommentDTO;
import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.boundry.dto.VideoDTO;
import com.FrameHopper.app.core.ports.in.comment.ChangeCommentContentCommand;
import com.FrameHopper.app.core.ports.in.comment.ChangeCommentListingOrderCommand;
import com.FrameHopper.app.core.ports.in.comment.CreateCommentCommand;
import com.FrameHopper.app.core.ports.in.comment.DeleteCommentCommand;
import com.FrameHopper.app.core.ports.in.video.VideoQuery;
import com.FrameHopper.app.ui.UiView;
import com.FrameHopper.app.ui.eventing.DeleteVideoEventDispatcher;
import com.FrameHopper.app.ui.eventing.DeleteVideoEventListener;
import com.FrameHopper.app.ui.eventing.VideoPathUpdatedEventDispatcher;
import com.FrameHopper.app.ui.eventing.VideoPathUpdatedListener;
import com.FrameHopper.app.ui.ve.NotesVideoTableEntry;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
@Scope("prototype")
public class NotesController implements UiView,
        DeleteVideoEventListener,
        VideoPathUpdatedListener
{
    @FXML
    private BorderPane notesView;
    @FXML
    private TextArea noteEditor;
    @FXML
    private ListView<NotesVideoTableEntry> notesList;
    @FXML
    private ImageView addNoteIcon, deleteNoteIcon;
    @FXML
    private HBox noteTabsBar;

    private final PauseTransition saveDebounce = new PauseTransition(Duration.millis(300));
    private final ToggleGroup toggleGroup = new ToggleGroup();

    private final VideoQuery videoQuery;
    private final ChangeCommentContentCommand changeCommentContentCommand;
    private final ChangeCommentListingOrderCommand changeCommentListingOrderCommand;
    private final CreateCommentCommand createCommentCommand;
    private final DeleteCommentCommand deleteCommentCommand;

    private CommentDTO currentNote;

    public NotesController(
            VideoQuery videoQuery,
            ChangeCommentContentCommand changeCommentContentCommand,
            ChangeCommentListingOrderCommand changeCommentListingOrderCommand,
            CreateCommentCommand createCommentCommand,
            DeleteCommentCommand deleteCommentCommand
    ) {
        this.videoQuery = videoQuery;
        this.changeCommentContentCommand = changeCommentContentCommand;
        this.changeCommentListingOrderCommand = changeCommentListingOrderCommand;
        this.createCommentCommand = createCommentCommand;
        this.deleteCommentCommand = deleteCommentCommand;

        DeleteVideoEventDispatcher.register(this);
        VideoPathUpdatedEventDispatcher.register(this);
    }

    @FXML
    public void initialize() {
        noteEditor.setPromptText(Dictionary.get("notes.empty-editor"));

        noteEditor.textProperty().addListener((obs, oldV, newV) -> {
            saveDebounce.stop();
            saveDebounce.playFromStart();
        });

        saveDebounce.setOnFinished(e -> saveCurrent());

        notesList.setCellFactory(createNotesCellFactory());
        notesList.getItems().addAll(videoQuery.getAllWithNotes().stream().map(NotesVideoTableEntry::new).toList());

        notesList.getSelectionModel().selectedItemProperty().addListener((obs, old, entry) -> {
            if(entry == null) return;
            currentNote = null;
            noteEditor.clear();
            loadCurrentTabs();
        });

        addNoteIcon.setImage(FXIconLoader.getLargeIcon("plus.png"));
        deleteNoteIcon.setImage(FXIconLoader.getLargeIcon("bin.png"));
    }

    private Callback<ListView<NotesVideoTableEntry>, ListCell<NotesVideoTableEntry>> createNotesCellFactory() {
        return lv -> new ListCell<>() {

            private final Label nameLabel = new Label();
            private final Label notesLabel = new Label();
            private final Label arrowLabel = new Label("›");

            private final VBox textBox = new VBox(2, nameLabel, notesLabel);
            private final Region spacer = new Region();
            private final HBox root = new HBox(10, textBox, spacer, arrowLabel);

            private NotesVideoTableEntry bound;

            {
                // Layout
                HBox.setHgrow(spacer, Priority.ALWAYS);
                root.setAlignment(Pos.CENTER_LEFT);
                root.setPadding(new Insets(6, 8, 6, 8));

                // Styling
                nameLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
                notesLabel.setStyle("-fx-font-size: 11; -fx-text-fill: -fx-text-inner-color;");
                arrowLabel.setStyle("-fx-font-size: 18; -fx-opacity: 0.6;");

                // Optional hover cue
                root.setStyle("""
                
                        -fx-background-radius: 6;
                """);

                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }

            @Override
            protected void updateItem(NotesVideoTableEntry item, boolean empty) {
                super.updateItem(item, empty);

                // Unbind previous
                if (bound != null) {
                    nameLabel.textProperty().unbind();
                    notesLabel.textProperty().unbind();
                    bound = null;
                }

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    bound = item;

                    nameLabel.textProperty().bind(item.nameProperty());
                    notesLabel.textProperty().bind(
                            item.notesCountProperty().asString(Dictionary.get("notes.amount"))
                    );

                    setGraphic(root);
                }
            }
        };
    }

    private void loadCurrentTabs() {
        noteTabsBar.getChildren().clear();
        toggleGroup.getToggles().clear();

        var notes = notesList.getSelectionModel().getSelectedItem().getVideo().comments();
        if(notes.isEmpty()) {
            currentNote = null;
            noteEditor.clear();
            return;
        }

        var sorted = notes.stream()
                .sorted(Comparator.comparingInt(CommentDTO::getListingOrder))
                .toList();

        sorted.forEach(n -> {
            var btn = new ToggleButton(Integer.toString(n.getListingOrder()));

            //styling
            btn.setMinWidth(Region.USE_PREF_SIZE);
            btn.setPrefWidth(Region.USE_COMPUTED_SIZE);
            btn.setMaxWidth(Region.USE_COMPUTED_SIZE);

            btn.setToggleGroup(toggleGroup);
            btn.setOnAction(e -> openNote(n));
            noteTabsBar.getChildren().add(btn);
        });

        openNote(sorted.getFirst());

        Platform.runLater(() -> {
            var stage = (Stage) notesView.getScene().getWindow();
            stage.setOnCloseRequest(e -> close());
        });
    }

    private void openNote(CommentDTO comment) {
        ((ToggleButton) noteTabsBar.getChildren().get(comment.getListingOrder()-1)).setSelected(true);
        currentNote = comment;
        noteEditor.setText(currentNote.getContent());
    }

    private void saveCurrent() {
        if(currentNote == null) return;

        if(currentNote.getContent().equals(noteEditor.getText())) return;

        currentNote.setContent(noteEditor.getText());
        var updated = changeCommentContentCommand.updateCommentContent(currentNote);
        currentNote.setContent(updated.getContent());
    }

    @FXML
    public void onAddNote() {
        var selected = notesList.getSelectionModel().getSelectedItem();
        if(selected == null) return;

        var notes = selected.getVideo().comments();
        var count = selected.getNotesCount();
        var created = createCommentCommand.CreateComment(new CommentDTO(
                -1,
                "",
                count + 1,
                selected.getVideo().id()
        ));
        notes.add(created);

        loadCurrentTabs();
        openNote(notes.getLast());

        selected.updateNotesCount();
    }

    @FXML
    public void onDeleteNote() {
        var selected = notesList.getSelectionModel().getSelectedItem();
        if(selected == null) return;

        if(currentNote == null) return;

        var comments = selected.getVideo().comments();
        if(comments.isEmpty()) return;

        deleteCommentCommand.DeleteComment(currentNote.getId());
        comments.remove(currentNote);

        if(!comments.isEmpty()) {
            var toUpdate = comments.stream()
                    .filter(c -> c.getListingOrder() > currentNote.getListingOrder())
                    .toList();

            toUpdate.forEach(c -> c.setListingOrder(c.getListingOrder() - 1));
            changeCommentListingOrderCommand.changeCommentListingOrder(toUpdate);
        }

        currentNote = null;
        selected.updateNotesCount();
        loadCurrentTabs();
    }

    @Override
    public void close() {
        DeleteVideoEventDispatcher.unregister(this);
        VideoPathUpdatedEventDispatcher.unregister(this);

        var stage = (Stage) notesView.getScene().getWindow();
        stage.close();
    }

    @Override
    public void onDeleteVideo(@NotNull VideoDTO videoDTO) {
        notesList.getItems().clear();
        notesList.getItems().addAll(videoQuery.getAllWithNotes().stream().map(NotesVideoTableEntry::new).toList());
    }

    @Override
    public void onVideoPathUpdated(@NotNull VideoDTO video) {
        var videos = notesList.getItems().stream().map(NotesVideoTableEntry::getVideo).toList();
        var index = videos.indexOf(video);
        if(index == -1) return;

        notesList.getItems().get(index).setName(video);
    }
}
