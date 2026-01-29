package com.FrameHopper.app.View.FXViews.Notes;

import com.FrameHopper.app.Model.Comment;
import com.FrameHopper.app.Model.Video;
import com.FrameHopper.app.View.Elements.DataManagers.ViewContainer.OpenViewsInformationContainer;
import com.FrameHopper.app.View.Elements.DataManagers.ViewContainer.ViewFlag;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXIconLoader;
import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.View.Elements.Language.LanguageChangeListener;
import com.FrameHopper.app.View.Elements.Language.LanguageManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@Component
@Scope("prototype")
public class NotesController implements LanguageChangeListener {
    @FXML
    private BorderPane notesView;
    @FXML
    private TextArea noteEditor;
    @FXML
    private ListView<TableEntry> notesList;
    @FXML
    private ImageView addNoteIcon, deleteNoteIcon;
    @FXML
    private HBox noteTabsBar;

    private final NotesService viewService;
    private final OpenViewsInformationContainer viewContainer;

    private final PauseTransition saveDebounce = new PauseTransition(Duration.millis(400));
    private final ToggleGroup toggleGroup = new ToggleGroup();
    private final Map<KeyCombination,Runnable> keyActions = new HashMap<>();

    private Video currentVideo;
    private Comment currentNote;

    public NotesController(NotesService noteService, OpenViewsInformationContainer viewContainer) {
        this.viewService = noteService;
        this.viewContainer = viewContainer;

        LanguageManager.register(this);
    }

    @FXML
    public void initialize(){
        //text area prompt
        noteEditor.setPromptText(Dictionary.get("notes.empty-editor"));

        //set autosave
        noteEditor.textProperty().addListener((obs, oldV, newV) -> {
            saveDebounce.stop();
            saveDebounce.playFromStart();
        });

        saveDebounce.setOnFinished(e -> saveCurrent());

        //populate notes list
        notesList.setCellFactory(createNotesCellFactory());
        notesList.getItems().addAll(
                        viewService.getVideosWithNotes().stream()
                        .map(TableEntry::new)
                        .toList()
        );

        //load notes tabs
        notesList.getSelectionModel().selectedItemProperty().addListener((obs, old, entry) -> {
            if(entry == null) return;
            currentVideo = entry.getVideo();
            currentNote = null;
            noteEditor.clear();
            loadCurrentTabs();
        });

        //button icons
        addNoteIcon.setImage(FXIconLoader.getLargeIcon("plus.png"));
        deleteNoteIcon.setImage(FXIconLoader.getLargeIcon("bin.png"));

        //handle closing
        Platform.runLater(() -> {
            var stage = (Stage) notesView.getScene().getWindow();
            stage.setOnCloseRequest(e -> {
                viewContainer.close(ViewFlag.NOTES);
                LanguageManager.unregister(this);
            });
        });

        //add key binds
        notesView.addEventFilter(KeyEvent.KEY_PRESSED,this::handleKeyPressed);

        keyActions.put(new KeyCodeCombination(KeyCode.N, KeyCombination.SHIFT_DOWN), this::close);
        keyActions.put(new KeyCodeCombination(KeyCode.DELETE), this::onDeleteNote);
        keyActions.put(new KeyCodeCombination(KeyCode.ADD), this::onAddNote);
        keyActions.put(new KeyCodeCombination(KeyCode.EQUALS), this::onAddNote);
    }

    //HANDLE KEY BINDS
    private void handleKeyPressed(KeyEvent event) {
        keyActions.keySet().stream()
                .filter(k -> k.match(event))
                .findFirst()
                .ifPresent(k -> {
                    event.consume();
                    keyActions.get(k).run();
                });
    }

    private void close(){
        LanguageManager.unregister(this);
        var stage = (Stage) notesView.getScene().getWindow();
        stage.close();
        viewContainer.close(ViewFlag.NOTES);
    }

    private Callback<ListView<TableEntry>, ListCell<TableEntry>> createNotesCellFactory() {
        return lv -> new ListCell<>() {

            private final Label nameLabel = new Label();
            private final Label notesLabel = new Label();
            private final Label arrowLabel = new Label("›");

            private final VBox textBox = new VBox(2, nameLabel, notesLabel);
            private final Region spacer = new Region();
            private final HBox root = new HBox(10, textBox, spacer, arrowLabel);

            private TableEntry bound;

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
            protected void updateItem(TableEntry item, boolean empty) {
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

        var notes = currentVideo.getComments();
        if(notes.isEmpty()) {
            currentNote = null;
            noteEditor.clear();
            return;
        }

        var sorted = notes.stream()
                .sorted(Comparator.comparingInt(Comment::getListingOrder))
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
    }

    private void openNote(Comment note) {
        ((ToggleButton) noteTabsBar.getChildren().get(note.getListingOrder()-1)).setSelected(true);
        currentNote = note;
        noteEditor.setText(currentNote.getContent());
    }

    private void saveCurrent() {
        if(currentNote != null)
            if(!currentNote.getContent().equals(noteEditor.getText())) {
                currentNote.setContent(noteEditor.getText());
                currentNote = viewService.save(currentNote);
            }
    }

    @FXML
    protected void onAddNote() {
        if(currentVideo != null) {
            var notes = currentVideo.getComments();
            var notesCount = (currentVideo.getComments() == null || currentVideo.getComments().isEmpty()) ? 0 : notes.size();
            viewService.save("", notesCount + 1, currentVideo);
            loadCurrentTabs();
            openNote(notes.getLast());
        }
    }

    @FXML
    protected void onDeleteNote() {
        if(currentNote != null) {
            viewService.delete(currentNote, currentVideo);
            currentVideo.getComments().remove(currentNote);

            if(!currentVideo.getComments().isEmpty())
                currentVideo.getComments().stream()
                    .filter(n -> n.getListingOrder() > currentNote.getListingOrder())
                    .forEach(n -> n.setListingOrder(n.getListingOrder()-1));

            currentNote = null;
            loadCurrentTabs();
        }
    }

    @Override
    public void changeLanguage() {
        //text area prompt
        noteEditor.setPromptText(Dictionary.get("notes.empty-editor"));

        //refresh list
        notesList.refresh();
    }
}
