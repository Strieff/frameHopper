package com.FrameHopper.app.ui.ve;

import com.FrameHopper.app.boundry.dto.VideoDTO;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;

public class VideoListTableEntry {
    @Getter
    private final VideoDTO video;

    private final SimpleStringProperty name;
    private final SimpleStringProperty path;

    public VideoListTableEntry(VideoDTO video) {
        this.video = video;
        this.name = new SimpleStringProperty(video.name());
        this.path = new SimpleStringProperty(video.path());
    }

    public String getName() {
        return name.get();
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
}
