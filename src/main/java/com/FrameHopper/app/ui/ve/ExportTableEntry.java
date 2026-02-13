package com.FrameHopper.app.ui.ve;

import com.FrameHopper.app.boundry.dto.FrameDTO;
import com.FrameHopper.app.boundry.dto.VideoDTO;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;

import java.util.List;

public class ExportTableEntry {
    @Getter
    private final VideoDTO video;
    @Getter
    private final List<FrameDTO> frames;

    private final StringProperty name;
    private final BooleanProperty selected;

    public ExportTableEntry(VideoDTO video, List<FrameDTO> frames) {
        this.video = video;
        this.frames = frames;

        name = new SimpleStringProperty(video.name());
        selected = new SimpleBooleanProperty(false);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }
}
