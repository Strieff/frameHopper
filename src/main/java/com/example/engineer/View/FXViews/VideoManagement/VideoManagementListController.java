package com.example.engineer.View.FXViews.VideoManagement;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class VideoManagementListController {

    @FXML
    private TableView<Path> codeTable;
    @FXML
    private TableColumn<Path, String> pathColumn;
    @FXML
    private TableColumn<Path, Void> selectColumn;
    @FXML
    private TableColumn<Path, Void> deleteColumn;

    public void initialize() {
        // Set up columns
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));

        // Set up checkbox for selectColumn
        selectColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Path, Void> call(final TableColumn<Path, Void> param) {
                return new TableCell<>() {
                    private final Button editButton = new Button("Edit");

                    {
                        editButton.setOnAction(event -> {
                            Path code = getTableView().getItems().get(getIndex());
                            getTableView().getItems().remove(code);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(editButton);
                        }
                    }
                };
            }
        });

        // Set up delete button for deleteColumn
        deleteColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Path, Void> call(final TableColumn<Path, Void> param) {
                return new TableCell<>() {
                    private final Button deleteButton = new Button("Delete");

                    {
                        deleteButton.setOnAction(event -> {
                            Path code = getTableView().getItems().get(getIndex());
                            getTableView().getItems().remove(code);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(deleteButton);
                        }
                    }
                };
            }
        });

        // Sample data
        ObservableList<Path> data = FXCollections.observableArrayList(
                new Path("test1"),
                new Path("test2")
        );

        // Add data to the TableView
        codeTable.setItems(data);
    }

    public static class Path {
        private final StringProperty path;

        public Path(String path) {
            this.path = new SimpleStringProperty(path);
        }

        public void setPath(String path) {
            this.path.set(path);
        }

        public String getPath() {
            return path.get();
        }
    }
}

