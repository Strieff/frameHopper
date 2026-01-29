package com.FrameHopper.app.View.FXViews.Charts;

import com.FrameHopper.app.Model.Video;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;
import lombok.Setter;

public class TableEntry {
    @Getter
    Video video;
    @Getter
    @Setter
    private boolean hasListener = false;

    SimpleStringProperty name;
    SimpleBooleanProperty selected;

    public TableEntry(Video video,String name) {
        this.video = video;
        this.name = new SimpleStringProperty(name);
        this.selected = new SimpleBooleanProperty(false);
    }

    public String getName() {
        return name.get();
    }

    public boolean isSelected() {
        return selected.get();
    }

    public SimpleBooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }
}
