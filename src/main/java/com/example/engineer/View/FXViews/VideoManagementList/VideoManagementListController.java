package com.example.engineer.View.FXViews.VideoManagementList;

import com.example.engineer.Service.VideoService;
import com.example.engineer.View.Elements.*;
import com.example.engineer.View.FXViews.MainView.MainViewService;
import com.example.engineer.View.FXViews.VideoDetails.VideoManagementDetailsController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

@Component
@Scope("prototype")
public class VideoManagementListController {
    @FXML
    private TableView<TableEntry> codeTable;
    @FXML
    private TableColumn<TableEntry, String> pathColumn;
    @FXML
    private TableColumn<TableEntry, Void> editColumn;
    @FXML
    private TableColumn<TableEntry, Void> deleteColumn;
    @FXML
    private BorderPane listView;

    @Autowired
    VideoManagementListService viewService;
    @Autowired
    MainViewService mainViewService;
    @Autowired
    OpenViewsInformationContainer openViews;

    private final Map<KeyCombination,Runnable> keyActions = new HashMap<>();
    @Autowired
    private VideoService videoService;

    public void initialize() {
        // Set up columns
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));

        // Set up edit column
        editColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<TableEntry, Void> call(final TableColumn<TableEntry, Void> param) {
                return new TableCell<>() {
                    private final Button editButton = new Button();
                    private final HBox centeredBox = new HBox(editButton);

                    {
                        editButton.setOnAction(event -> {
                            TableEntry video = getTableView().getItems().get(getIndex());
                            onEdit(video.getId());
                        });
                        editButton.setGraphic(new ImageView(FXIconLoader.getSmallIcon("edit.png")));

                        // Center-align the button within the HBox
                        centeredBox.setAlignment(Pos.CENTER);
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

        // Set up delete button
        deleteColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<TableEntry, Void> call(final TableColumn<TableEntry, Void> param) {
                return new TableCell<>() {
                    private final Button editButton = new Button();
                    private final HBox centeredBox = new HBox(editButton);

                    {
                        editButton.setOnAction(event -> {
                            TableEntry video = getTableView().getItems().get(getIndex());
                            onDelete(video.getId());
                        });
                        editButton.setGraphic(new ImageView(FXIconLoader.getSmallIcon("bin.png")));

                        // Center-align the button within the HBox
                        centeredBox.setAlignment(Pos.CENTER);
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

        // Add data to the TableView
        codeTable.setItems(viewService.getAllVideos());

        //kye binds
        keyActions.put(new KeyCodeCombination(KeyCode.L, KeyCombination.SHIFT_DOWN), this::onShiftLPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.D, KeyCombination.SHIFT_DOWN), this::onShiftDPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN), this::onCtrlXPressed);

        //add key binds
        listView.addEventFilter(KeyEvent.KEY_PRESSED,this::handleKeyPressed);

        Platform.runLater(() -> {
            var stage = (Stage) codeTable.getScene().getWindow();
            stage.setOnCloseRequest(e -> openViews.closeVideoList());
        });
    }

    private void handleKeyPressed(KeyEvent event){
        keyActions.keySet().stream()
                .filter(k -> k.match(event))
                .findFirst()
                .ifPresent(k -> keyActions.get(k).run());
    }

    //CLOSE VIDEO LIST
    private void onShiftLPressed() {
        var stage = (Stage) listView.getScene().getWindow();
        stage.close();
        openViews.closeVideoList();
    }

    //OPEN CURRENT OR SELECTED
    private void onShiftDPressed() {
        if (mainViewService.isOpen())
            openDetails(mainViewService.getCurrentId());
        else {
            var selected = codeTable.getSelectionModel();
            if (selected.getSelectedItem() != null)
                openDetails(selected.getSelectedItem().getId());
            else
                FXDialogProvider.errorDialog("No video to open");
        }
    }

    private void onEdit(int id){
        openDetails(id);
    }

    private void openDetails(int id){
        try{
            var loader = FXMLViewLoader.getView("VideoManagementDetailsViewModel");

            //load scene
            Parent root = loader.load();
            var videoDetailsScene = new Scene(root);

            //get controller
            VideoManagementDetailsController videoController = loader.getController();
            videoController.init(viewService.getVideo(id));

            //new stage
            var secondaryStage = new Stage();
            secondaryStage.setScene(videoDetailsScene);
            secondaryStage.setTitle("Video Details");

            //make it a modal window
            secondaryStage.initOwner(listView.getScene().getWindow());
            secondaryStage.show();
        }catch (Exception e){
            FXDialogProvider.errorDialog(e.getMessage());
        }
    }

    //DELETE VIDEO
    private void onCtrlXPressed() {
        var selected = codeTable.getSelectionModel().getSelectedItem();

        if(selected!=null){
            deleteVideo(selected.getId());
        }else
            FXDialogProvider.errorDialog("No video selected");
    }

    private void onDelete(int id){
        deleteVideo(id);
    }

    private void deleteVideo(int id){
        if(FXDialogProvider.YesNoDialog("Do you want to delete: "+ videoService.getById(id).getName()+"?")) {
            videoService.deleteVideo(id);
            codeTable.getItems().remove(IntStream.range(0, codeTable.getItems().size()).filter(i -> codeTable.getItems().get(i).getId() == id).findFirst().orElse(-1));
        }else
            return;

        if(id == mainViewService.getCurrentId())
            FXRestartResolver.reset();
    }
}

