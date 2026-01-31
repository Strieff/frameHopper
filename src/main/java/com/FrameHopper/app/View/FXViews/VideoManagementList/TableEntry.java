package com.FrameHopper.app.View.FXViews.VideoManagementList;

import com.FrameHopper.app.Model.Video;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;

public class TableEntry {
    @Getter
    private final Video video;

    private final SimpleStringProperty name;
    private final SimpleStringProperty path;

    public TableEntry(Video video) {
        this.video = video;
        this.name = new SimpleStringProperty(video.getName().replace("%20", " "));
        this.path = new SimpleStringProperty(video.getPath());
    }

    public String getName() {
        return name.get().replace("%20", " ");
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public String getPath() {
        return path.get();
    }

    public SimpleStringProperty pathProperty() {
        return path;
    }

    public int getId() {
        return video.getId();
    }
}
