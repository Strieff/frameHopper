package com.FrameHopper.app.ui.controller;

import com.FrameHopper.app.View.Elements.FXElementsProviders.FXIconLoader;
import com.FrameHopper.app.boundry.dto.VideoDTO;
import com.FrameHopper.app.core.ports.in.video.DeleteVideoCommand;
import com.FrameHopper.app.core.ports.in.video.VideoQuery;
import com.FrameHopper.app.ui.FXMLViewLoader;
import com.FrameHopper.app.ui.UiView;
import com.FrameHopper.app.ui.eventing.*;
import com.FrameHopper.app.ui.ve.VideoListTableEntry;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class VideoListController extends UiView implements
        DeleteVideoEventListener,
        VideoPathUpdatedListener
{
    @FXML
    private ListView<VideoListTableEntry> videoList;
    @FXML
    private BorderPane listView;

    private final VideoQuery videoQuery;
    private final DeleteVideoCommand deleteVideoCommand;

    public VideoListController(
            VideoQuery videoQuery,
            DeleteVideoCommand deleteVideoCommand
    ) {
        this.videoQuery = videoQuery;
        this.deleteVideoCommand = deleteVideoCommand;

        DeleteVideoEventDispatcher.register(this);
        VideoPathUpdatedEventDispatcher.register(this);

    }

    @FXML
    private void initialize() {
        videoList.setCellFactory(createVideoListCellFactory());

        loadTable();

        Platform.runLater(() -> {
            var stage = (Stage) listView.getScene().getWindow();
            stage.setOnCloseRequest(e -> close());
        });
    }

    private void loadTable() {
        var videos = videoQuery.getAllVideos();

        if(videos != null && !videos.isEmpty()) {
            videoList.getItems().clear();
            videoList.getItems().addAll(videos.stream().map(VideoListTableEntry::new).toList());
        }
    }

    private Callback<ListView<VideoListTableEntry>, ListCell<VideoListTableEntry>> createVideoListCellFactory() {
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

            private VideoListTableEntry bound;

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

                openButton.setOnAction(e -> OpenVideoEventDispatcher.dispatch(bound.getVideo().id()));
                deleteButton.setOnAction(e -> {
                    deleteVideoCommand.deleteVideo(bound.getVideo().id());
                    DeleteVideoEventDispatcher.dispatch(bound.getVideo());
                });
                editButton.setOnAction(e -> {
                    var loader = FXMLViewLoader.getView(
                            "VideoManagementDetailsViewModel",
                            "Video Details",
                            listView
                    );

                    VideoDetailsController controller = loader.getController();
                    controller.init(bound.getVideo());
                });
            }

            @Override
            protected void updateItem(VideoListTableEntry item, boolean empty) {
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

    @Override
    public void addKeybinds() {

    }

    @Override
    public void handleKeyPressed(KeyEvent event) {

    }

    @Override
    public void close() {
        DeleteVideoEventDispatcher.unregister(this);
        VideoPathUpdatedEventDispatcher.unregister(this);

        var stage = (Stage) listView.getScene().getWindow();
        stage.close();
    }

    @Override
    public void onDeleteVideo(VideoDTO videoDTO) {
        loadTable();
    }

    @Override
    public void onVideoPathUpdated(VideoDTO video) {
        loadTable();
    }
}
