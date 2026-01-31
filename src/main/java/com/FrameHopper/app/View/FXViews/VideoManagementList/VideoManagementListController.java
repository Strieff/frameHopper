package com.FrameHopper.app.View.FXViews.VideoManagementList;

import com.FrameHopper.app.View.Elements.DataManagers.ViewContainer.OpenViewsInformationContainer;
import com.FrameHopper.app.View.Elements.DataManagers.ViewContainer.ViewFlag;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXDialogProvider;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXIconLoader;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXMLViewLoader;
import com.FrameHopper.app.View.Elements.FXElementsProviders.RestartResolver;
import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.View.Elements.Language.LanguageChangeListener;
import com.FrameHopper.app.View.Elements.Language.LanguageManager;
import com.FrameHopper.app.View.Elements.OpenVideo.OpenVideoEventDispatcher;
import com.FrameHopper.app.View.Elements.UpdateTableEvent.UpdateTableEventDispatcher;
import com.FrameHopper.app.View.Elements.UpdateTableEvent.UpdateTableListener;
import com.FrameHopper.app.View.FXViews.MainView.MainViewService;
import com.FrameHopper.app.View.FXViews.VideoDetails.VideoManagementDetailsController;
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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope("prototype")
public class VideoManagementListController implements LanguageChangeListener, UpdateTableListener {
    @FXML
    private ListView<TableEntry> videoList;
    @FXML
    private BorderPane listView;

    private final VideoManagementListService viewService;
    private final MainViewService mainViewService;
    private final OpenViewsInformationContainer viewContainer;

    private final Map<KeyCombination,Runnable> keyActions = new HashMap<>();

    public VideoManagementListController(
            VideoManagementListService viewService,
            MainViewService mainViewService,
            OpenViewsInformationContainer viewContainer
    ) {
        this.viewService = viewService;
        this.mainViewService = mainViewService;
        this.viewContainer = viewContainer;

        LanguageManager.register(this);
        UpdateTableEventDispatcher.register(this);
    }

    public void initialize() {
        videoList.setCellFactory(createVideoListCellFactory());
        videoList.getItems().addAll(viewService.getAllVideos());

        //kye binds
        keyActions.put(new KeyCodeCombination(KeyCode.L, KeyCombination.SHIFT_DOWN), this::close);
        keyActions.put(new KeyCodeCombination(KeyCode.D, KeyCombination.SHIFT_DOWN), this::onShiftDPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN), this::onCtrlXPressed);

        //add key binds
        listView.addEventFilter(KeyEvent.KEY_PRESSED,this::handleKeyPressed);

        Platform.runLater(() -> {
            var stage = (Stage) listView.getScene().getWindow();
            stage.setOnCloseRequest(e -> close());
        });
    }

    private Callback<ListView<TableEntry>, ListCell<TableEntry>> createVideoListCellFactory() {
        return lv -> new ListCell<>() {

            private final Label nameLabel = new Label();
            private final Label pathLabel = new Label();

            private final Button openButton = new Button();
            private final Button deleteButton = new Button();
            private final Button editButton = new Button();

            private final VBox textBox = new VBox(2, nameLabel, pathLabel);
            private final Region spacer = new Region();
            private final HBox buttons = new HBox(3, openButton, deleteButton, editButton);
            private final HBox root = new HBox(10, textBox, spacer, buttons);

            private TableEntry bound;

            {
                // Layout
                HBox.setHgrow(spacer, Priority.ALWAYS);
                root.setAlignment(Pos.CENTER_LEFT);
                root.setPadding(new Insets(6, 8, 6, 8));

                // Styling
                nameLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
                pathLabel.setStyle("-fx-font-size: 11; -fx-text-fill: -fx-text-inner-color;");
                pathLabel.setWrapText(true);
                pathLabel.setPrefWidth(300);

                // Optional hover cue
                root.setStyle("""
                        -fx-background-radius: 6;
                """);

                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

                //button icons
                openButton.setGraphic(new ImageView(FXIconLoader.getSmallIcon("open-folder.png")));
                deleteButton.setGraphic(new ImageView(FXIconLoader.getSmallIcon("bin.png")));
                editButton.setGraphic(new ImageView(FXIconLoader.getSmallIcon("edit.png")));

                //button functionality
                openButton.setOnAction(e -> OpenVideoEventDispatcher.fireEvent(bound.getId()));
                deleteButton.setOnAction(e -> deleteVideo(bound));
                editButton.setOnAction(e -> edit(bound));
            }

            @Override
            protected void updateItem(TableEntry item, boolean empty) {
                super.updateItem(item, empty);

                // Unbind previous
                if (bound != null) {
                    nameLabel.textProperty().unbind();
                    pathLabel.textProperty().unbind();
                    bound = null;
                }

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    bound = item;

                    nameLabel.textProperty().bind(item.nameProperty());
                    pathLabel.textProperty().bind(item.pathProperty());

                    setGraphic(root);
                }
            }
        };
    }

    public void close(){
        LanguageManager.unregister(this);
        UpdateTableEventDispatcher.unregister(this);

        var stage = (Stage) listView.getScene().getWindow();
        viewContainer.close(ViewFlag.VIDEO_LIST);
        stage.close();
    }

    private void handleKeyPressed(KeyEvent event){
        keyActions.keySet().stream()
                .filter(k -> k.match(event))
                .findFirst()
                .ifPresent(k -> keyActions.get(k).run());
    }

    private void onShiftDPressed(){
        var selected = videoList.getSelectionModel().getSelectedItem();

        if(selected != null)
            edit(selected);
    }

    private void onCtrlXPressed() {
        var selected = videoList.getSelectionModel().getSelectedItem();

        if(selected != null)
            deleteVideo(selected);
    }

    private void edit(TableEntry entry){
        var loader = FXMLViewLoader.getView(
                "VideoManagementDetailsViewModel",
                "Video Details",
                listView
        );

        //get controller
        VideoManagementDetailsController videoController = loader.getController();
        videoController.init(entry.getVideo());
    }

    private void deleteVideo(TableEntry entry){
        if(FXDialogProvider.yesNoDialog(String.format(Dictionary.get("warning.video.delete"), entry.getName()))) {
            viewService.deleteVideo(entry.getId());
            videoList.getItems().remove(entry);
        }else
            return;

        if(entry.getId() == mainViewService.getCurrentId())
            RestartResolver.reset();
    }

    @Override
    public void changeLanguage() {
        //codeTable.setPlaceholder(new Label(Dictionary.get("placeholder.video")));
    }

    @Override
    public void updateTable() {
        videoList.getItems().clear();
        videoList.getItems().addAll(viewService.getAllVideos());
    }
}

