package com.example.engineer.View.FXViews.TagManager;

import com.example.engineer.View.Elements.Language.Dictionary;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class TagManagerController {
    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private TableView<TableEntry> codeTable;

    @FXML
    private TableColumn<TableEntry, Boolean> selectColumn;

    @FXML
    private TableColumn<TableEntry, String> codeColumn;

    @FXML
    private TableColumn<TableEntry, Double> valueColumn;

    @FXML
    private Button cancelButton;

    @FXML
    private Button saveButton;

    @FXML
    private Label frameLabel;
    @FXML
    private BorderPane tagManagerView;

    private final ObservableList<TableEntry> items = FXCollections.observableArrayList();

    @Autowired
    TagManagerService viewService;

    @FXML
    public void initialize() {
        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));

        codeColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));



        // Set up button actions
        cancelButton.setOnAction(event -> handleCancel());
        saveButton.setOnAction(event -> handleSave());
        searchButton.setOnAction(event -> handleSearch());

        Platform.runLater(() -> {
            tagManagerView.requestFocus();
        });
    }

    public void init(String videoPath, int frameNo){
        frameLabel.setText(String.format(
                Dictionary.get("tm.frame"),
                (frameNo + 1)
        ));

        // Populate the table with data
        items.addAll(viewService.getTags());
        codeTable.setItems(items);
    }

    private void handleCancel() {
        System.out.println("Cancel button clicked");
        // Handle cancel action
    }

    private void handleSave() {
        System.out.println("Save button clicked");
        // Handle save action
    }

    private void handleSearch() {
        String searchText = searchField.getText();
        System.out.println("Searching for: " + searchText);
        // Handle search functionality
    }
}
