package com.FrameHopper.app.ui.ve;

import com.FrameHopper.app.boundry.dto.FrameDTO;
import com.FrameHopper.app.boundry.dto.VideoDTO;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;

import java.util.List;

public class ChartsTableEntry {
    @Getter
    private final VideoDTO video;
    @Getter
    private final List<FrameDTO> frames;

    private final SimpleStringProperty name;
    private final BooleanProperty selected;

    public ChartsTableEntry(VideoDTO video, List<FrameDTO> frames) {
        this.video = video;
        this.frames = frames;

        this.name = new SimpleStringProperty(video.name());
        this.selected = new SimpleBooleanProperty(false);
    }

    public String getName() {
        return name.get();
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }
}
