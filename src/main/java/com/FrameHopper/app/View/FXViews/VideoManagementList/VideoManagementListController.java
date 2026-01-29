package com.FrameHopper.app.View.FXViews.VideoManagementList;

import com.FrameHopper.app.Service.VideoService;
import com.FrameHopper.app.View.Elements.DataManagers.ViewContainer.OpenViewsInformationContainer;
import com.FrameHopper.app.View.Elements.DataManagers.ViewContainer.ViewFlag;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXDialogProvider;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXIconLoader;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXMLViewLoader;
import com.FrameHopper.app.View.Elements.FXElementsProviders.RestartResolver;
import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.View.Elements.Language.LanguageChangeListener;
import com.FrameHopper.app.View.Elements.Language.LanguageManager;
import com.FrameHopper.app.View.FXViews.MainView.MainViewService;
import com.FrameHopper.app.View.FXViews.VideoDetails.VideoManagementDetailsController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

@Component
@Scope("prototype")
public class VideoManagementListController implements LanguageChangeListener {
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

    private final VideoManagementListService viewService;
    private final MainViewService mainViewService;
    private final OpenViewsInformationContainer viewContainer;
    private final VideoService videoService;

    private final Map<KeyCombination,Runnable> keyActions = new HashMap<>();

    public VideoManagementListController(
            VideoManagementListService viewService,
            MainViewService mainViewService,
            OpenViewsInformationContainer viewContainer,
            VideoService videoService
    ) {
        this.viewService = viewService;
        this.mainViewService = mainViewService;
        this.viewContainer = viewContainer;
        this.videoService = videoService;

        LanguageManager.register(this);
    }

    public void initialize() {
        codeTable.setPlaceholder(new Label(Dictionary.get("placeholder.video")));

        // Set up columns
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        pathColumn.setText(Dictionary.get("vl.path"));
        pathColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<TableEntry, String> call(TableColumn<TableEntry, String> param) {
                return new TableCell<>() {
                    private final Text text = new Text();

                    {
                        text.wrappingWidthProperty().bind(pathColumn.widthProperty());
                        setGraphic(text);
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            text.setText(null);
                        }else{
                            text.setText(item);
                        }
                    }
                };
            }
        });

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
            stage.setOnCloseRequest(e -> {
                LanguageManager.unregister(this);
                close();
            });
        });
    }

    public void close(){
        var stage = (Stage) codeTable.getScene().getWindow();
        viewContainer.close(ViewFlag.VIDEO_LIST);
        stage.close();
    }

    private void handleKeyPressed(KeyEvent event){
        keyActions.keySet().stream()
                .filter(k -> k.match(event))
                .findFirst()
                .ifPresent(k -> keyActions.get(k).run());
    }

    //CLOSE VIDEO LIST
    private void onShiftLPressed() {
        LanguageManager.unregister(this);
        var stage = (Stage) listView.getScene().getWindow();
        stage.close();
        viewContainer.close(ViewFlag.VIDEO_LIST);
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
                FXDialogProvider.errorDialog(Dictionary.get("error.vl.current"));
        }
    }

    private void onEdit(int id){
        openDetails(id);
    }

    private void openDetails(int id){
        var loader = FXMLViewLoader.getView(
                "VideoManagementDetailsViewModel",
                "Video Details",
                listView
        );

        //get controller
        VideoManagementDetailsController videoController = loader.getController();
        videoController.init(viewService.getVideo(id),this);
    }

    //DELETE VIDEO
    private void onCtrlXPressed() {
        var selected = codeTable.getSelectionModel().getSelectedItem();

        if(selected!=null){
            deleteVideo(selected.getId());
        }else
            FXDialogProvider.errorDialog(Dictionary.get("error.vl.selected"));
    }

    private void onDelete(int id){
        deleteVideo(id);
    }

    private void deleteVideo(int id){
        if(FXDialogProvider.yesNoDialog(String.format(Dictionary.get("message.vl.delete"),videoService.getById(id).getName()))) {
            videoService.deleteVideo(id);
            codeTable.getItems().remove(IntStream.range(0, codeTable.getItems().size()).filter(i -> codeTable.getItems().get(i).getId() == id).findFirst().orElse(-1));
        }else
            return;

        if(id == mainViewService.getCurrentId())
            RestartResolver.reset();
    }

    @Override
    public void changeLanguage() {
        pathColumn.setText(Dictionary.get("vl.path"));
        codeTable.setPlaceholder(new Label(Dictionary.get("placeholder.video")));
    }
}

