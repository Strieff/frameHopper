package com.example.engineer.View.FXViews.VideoMerge;

import com.example.engineer.Model.Video;
import com.example.engineer.View.Elements.FXElementsProviders.RestartResolver;
import com.example.engineer.View.Elements.Language.Dictionary;
import com.example.engineer.View.Elements.Language.LanguageChangeListener;
import com.example.engineer.View.Elements.Language.LanguageManager;
import com.example.engineer.View.FXViews.MainView.MainViewService;
import com.example.engineer.View.FXViews.VideoManagementList.VideoManagementListController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class FrameDataMergeController implements LanguageChangeListener {
    @FXML
    private BorderPane frameDataComparisonView;
    @FXML
    private Label conflictLabel,titleLabel,oldLabel,newLabel;
    @FXML
    private Button cancelButton,finishButton,oldSelectAll,oldSelectDistinct,newSelectAll,newSelectDistinct;
    @FXML
    private TableView<TableEntry> oldFramesTable,newFramesTable;
    @FXML
    private TableColumn<TableEntry,Boolean> oldCheckboxColumn,newCheckboxColumn;
    @FXML
    private TableColumn<TableEntry,Integer> oldFrameNoColumn,oldTagAmount,newFrameNoColumn,newTagAmount;

    @Autowired
    private VideoMergeService viewService;
    @Autowired
    private MainViewService mainViewService;

    private final ObservableList<TableEntry> oldFrames = FXCollections.observableArrayList();
    private final ObservableList<TableEntry> newFrames = FXCollections.observableArrayList();

    private Video oldVideo,newVideo;
    private boolean allOldSelected = false;
    private boolean allNewSelected = false;
    private VideoManagementListController parent;

    @FXML
    public void initialize() {
        LanguageManager.register(this);

        titleLabel.setText(Dictionary.get("fv.frame-data"));

        oldLabel.setText(Dictionary.get("fv.old-data"));
        newLabel.setText(Dictionary.get("fv.new-data"));

        //BUTTONS
        cancelButton.setText(Dictionary.get("cancel"));
        cancelButton.setOnAction(event -> closeWindow());

        finishButton.setText(Dictionary.get("continue"));
        finishButton.setOnAction(event -> mergeData());

        //OLD DATA BUTTONS
        oldSelectAll.setText(Dictionary.get("fv.select-all"));
        oldSelectAll.setOnAction(event -> {
            if(!allOldSelected)
                selectAll(oldFramesTable, oldFrames);
            else
                deselectAll(oldFramesTable,oldFrames);

            allOldSelected = !allOldSelected;
        });

        oldSelectDistinct.setText(Dictionary.get("fv.select-distinct"));
        oldSelectDistinct.setOnAction(event -> selectDistinct());

        //NEW DATA BUTTONS
        newSelectAll.setText(Dictionary.get("fv.select-all"));
        newSelectAll.setOnAction(event -> {
            if(!allNewSelected)
                selectAll(newFramesTable,newFrames);
            else
                deselectAll(newFramesTable,newFrames);

            allNewSelected = !allNewSelected;
        });

        newSelectDistinct.setText(Dictionary.get("fv.select-distinct"));
        newSelectDistinct.setOnAction(event -> selectDistinct());

        //OLD DATA TABLE
        oldFramesTable.setPlaceholder(new Label(Dictionary.get("fv.no-data")));
        oldFramesTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(TableEntry item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty)
                    setStyle("");
                else if (!item.isSelectable())
                    setStyle("-fx-background-color: red;");
                else
                    setStyle("");
            }
        });

        oldCheckboxColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        oldCheckboxColumn.setCellFactory(CheckBoxTableCell.forTableColumn((Integer index) -> {
            TableEntry entry = oldFramesTable.getItems().get(index);

            if(!entry.isHasListener()){
                entry.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if(entry.isSelectable())
                        entry.setSelected(newValue);
                    else
                        entry.setSelected(false);
                });
                entry.setHasListener(true);
            }

            return entry.selectedProperty();
        }));

        oldFrameNoColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        oldFrameNoColumn.setText(Dictionary.get("fv.frame-no"));

        oldTagAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        oldTagAmount.setText(Dictionary.get("fv.count"));

        //NEW DATA TABLE
        newFramesTable.setPlaceholder(new Label(Dictionary.get("fv.no-data")));
        newFramesTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(TableEntry item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty)
                    setStyle("");
                else if (!item.isSelectable())
                    setStyle("-fx-background-color: red;");
                else
                    setStyle("");
            }
        });

        newCheckboxColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        newCheckboxColumn.setCellFactory(CheckBoxTableCell.forTableColumn((Integer index) -> {
            TableEntry entry = newFramesTable.getItems().get(index);

            if(!entry.isHasListener()){
                entry.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    entry.setSelected(newValue);
                });
                entry.setHasListener(true);
            }

            return entry.selectedProperty();
        }));

        newFrameNoColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        newFrameNoColumn.setText(Dictionary.get("fv.frame-no"));

        newTagAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        newTagAmount.setText(Dictionary.get("fv.count"));

        Platform.runLater(() -> {
            var stage = (Stage) frameDataComparisonView.getScene().getWindow();
            stage.setOnCloseRequest(e -> LanguageManager.unregister(this));
        });
    }



    public void init(Video oldVideo, Video newVideo, VideoManagementListController parent) {
        this.oldVideo = oldVideo;
        this.newVideo = newVideo;
        this.parent = parent;

        oldFrames.addAll(viewService.getFrames(oldVideo));
        oldFramesTable.setItems(oldFrames);

        newFrames.addAll(viewService.getFrames(newVideo));
        newFramesTable.setItems(newFrames);

        conflictLabel.setText(viewService.compareFrameData(oldVideo,newVideo));
        if(!conflictLabel.getText().equals(Dictionary.get("mv.metadata.no-conflict")))
            conflictLabel.setTextFill(Color.RED);

        if(newFrames.isEmpty() && oldFrames.isEmpty())
            return;

        //block overflow frames on old video
        if(viewService.doesOldHaveMoreFrames(oldVideo,newVideo))
            viewService.blockOverflow(oldFrames,newVideo);

        if(!oldFrames.isEmpty() && newFrames.isEmpty())
            selectAll(oldFramesTable,oldFrames);

        if(oldFrames.isEmpty() && !newFrames.isEmpty())
            selectAll(newFramesTable,newFrames);
    }

    private void mergeData(){
        if(viewService.getMergeConfirmation()){
            var mergedVideo = viewService.mergeData(
                    oldFramesTable.getItems(),
                    oldVideo,
                    newFramesTable.getItems(),
                    newVideo
            );

            if (mergedVideo != null)
                if(mergedVideo.getId() == mainViewService.getCurrentId())
                    RestartResolver.reset();

            parent.close();
        }
    }

    private void selectAll(TableView<TableEntry> framesTable, ObservableList<TableEntry> frameList) {
        if(!framesTable.getItems().isEmpty())
            for(var e : framesTable.getItems())
                if(!e.selectedProperty().get() && e.selectable)
                    e.setSelected(true);
    }

    private void deselectAll(TableView<TableEntry> framesTable, ObservableList<TableEntry> frameList) {
        if(!framesTable.getItems().isEmpty()) {
            for (var e : framesTable.getItems())
                e.setSelected(false);
        }

    }

    private void selectDistinct() {

    }

    private void closeWindow() {
        LanguageManager.unregister(this);
        Stage stage = (Stage) frameDataComparisonView.getScene().getWindow();
        stage.close();
    }

    @Override
    public void changeLanguage() {
        titleLabel.setText(Dictionary.get("fv.frame-data"));

        oldLabel.setText(Dictionary.get("fv.old-data"));
        newLabel.setText(Dictionary.get("fv.new-data"));

        cancelButton.setText(Dictionary.get("cancel"));
        finishButton.setText(Dictionary.get("continue"));

        oldSelectAll.setText(Dictionary.get("fv.select-all"));
        oldSelectDistinct.setText(Dictionary.get("fv.select-distinct"));

        newSelectAll.setText(Dictionary.get("fv.select-all"));
        newSelectDistinct.setText(Dictionary.get("fv.select-distinct"));

        oldFramesTable.setPlaceholder(new Label(Dictionary.get("fv.no-data")));
        newFramesTable.setPlaceholder(new Label(Dictionary.get("fv.no-data")));

        oldFrameNoColumn.setText(Dictionary.get("fv.frame-no"));
        oldTagAmount.setText(Dictionary.get("fv.count"));

        newFrameNoColumn.setText(Dictionary.get("fv.frame-no"));
        newTagAmount.setText(Dictionary.get("fv.count"));

        conflictLabel.setText(viewService.compareFrameData(oldVideo,newVideo));
        if(!conflictLabel.getText().equals(Dictionary.get("mv.metadata.no-conflict")))
            conflictLabel.setTextFill(Color.RED);
        else
            conflictLabel.setTextFill(Color.BLACK);
    }
}
