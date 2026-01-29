package com.FrameHopper.app.View.FXViews.VideoManagementList;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;

public class TableEntry {
    @Getter
    private final int id;
    private final StringProperty path;

    public TableEntry(int id, String path) {
        this.id = id;
        this.path = new SimpleStringProperty(path);
    }

    public String getPath() {
        return path.get();
    }
}
