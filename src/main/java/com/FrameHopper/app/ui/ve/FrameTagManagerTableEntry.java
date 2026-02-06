package com.FrameHopper.app.ui.ve;

import com.FrameHopper.app.boundry.dto.TagDTO;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;

@Getter
public class FrameTagManagerTableEntry {
    private final TagDTO tag;

    private final BooleanProperty selected;
    private final SimpleStringProperty name;
    private final SimpleDoubleProperty value;

    public FrameTagManagerTableEntry(TagDTO tag) {
        this.tag = tag;
        this.selected = new SimpleBooleanProperty(false);
        this.name = new SimpleStringProperty(tag.getName());
        this.value = new SimpleDoubleProperty(tag.getValue());
    }

    public String getName() {
        return name.get();
    }

    public double getValue() {
        return value.get();
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }
}
