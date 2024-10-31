package com.example.engineer.View.FXViews.ExportView;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.KeyCode;

import java.util.List;

public class ExportController {

    @FXML
    private TableView<VideoItem> videoTable;
    @FXML
    private TableColumn<VideoItem, Boolean> selectColumn;
    @FXML
    private TableColumn<VideoItem, String> videoNameColumn;
    @FXML
    private TextField searchField;
    @FXML
    private Button clearButton, cancelButton, exportButton;

    private final FilteredList<VideoItem> filteredItems;
    private int lastSelectedIndex = -1;

    public ExportController() {
        // Sample data
        List<VideoItem> videoList = List.of(
                new VideoItem("Fmab_odc_1_shot_do_analizy_00003981.mp4"),
                new VideoItem("Fmab_odc_1_shot_do_analizy_00004040.mp4"),
                new VideoItem("MOV_0404.mp4"),
                new VideoItem("download-failed.gif"),
                new VideoItem("download-failed — kopia.gif"),
                new VideoItem("Untitled2.mp4"),
                new VideoItem("Zabotest.mp4")
        );
        filteredItems = new FilteredList<>(FXCollections.observableArrayList(videoList));
    }

    @FXML
    public void initialize() {
        videoTable.setItems(filteredItems);
        videoNameColumn.setCellValueFactory(cellData -> cellData.getValue().videoNameProperty());
        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());

        // Add checkbox column with factory to allow selection
        selectColumn.setCellFactory(tc -> {
            CheckBoxTableCell<VideoItem, Boolean> cell = new CheckBoxTableCell<>();
            cell.setOnMouseClicked(event -> handleShiftSelect(cell.getIndex()));
            return cell;
        });

        // Add listener to filter items based on search field
        searchField.textProperty().addListener((observable, oldValue, newValue) ->
                filteredItems.setPredicate(item -> item.getVideoName().toLowerCase().contains(newValue.toLowerCase())));

        // Clear button action to deselect all checkboxes
        clearButton.setOnAction(event -> videoTable.getItems().forEach(item -> item.setSelected(false)));
    }

    private void handleShiftSelect(int currentIndex) {
        if (lastSelectedIndex >= 0 && lastSelectedIndex != currentIndex && isShiftPressed()) {
            int start = Math.min(lastSelectedIndex, currentIndex);
            int end = Math.max(lastSelectedIndex, currentIndex);
            for (int i = start; i <= end; i++) {
                videoTable.getItems().get(i).setSelected(true);
            }
        }
        lastSelectedIndex = currentIndex;
    }

    private boolean isShiftPressed() {
        return searchField.getScene().getAccelerators().containsKey(KeyCode.SHIFT);
    }

    public static class VideoItem {
        private final SimpleStringProperty videoName;
        private final SimpleBooleanProperty selected;

        public VideoItem(String videoName) {
            this.videoName = new SimpleStringProperty(videoName);
            this.selected = new SimpleBooleanProperty(false);
        }

        public String getVideoName() {
            return videoName.get();
        }

        public SimpleStringProperty videoNameProperty() {
            return videoName;
        }

        public boolean isSelected() {
            return selected.get();
        }

        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }

        public SimpleBooleanProperty selectedProperty() {
            return selected;
        }
    }
}