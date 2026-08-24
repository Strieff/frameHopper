package com.FrameHopper.app.ui.ve;

import com.FrameHopper.app.boundry.dto.TagDTO;
import javafx.beans.property.*;
import lombok.Getter;

public class TagManagerTableEntry {
    @Getter
    private TagDTO tag;

    private final StringProperty code;
    private final DoubleProperty value;
    private final StringProperty description;
    private final BooleanProperty visible;

    public TagManagerTableEntry(TagDTO tag) {
        this.tag = tag;
        this.code = new SimpleStringProperty(tag.getName());
        this.value = new SimpleDoubleProperty(tag.getValue());
        this.description = new SimpleStringProperty(tag.getDescription());
        this.visible = new SimpleBooleanProperty(tag.getVisible());
    }

    public void setTag(TagDTO tag) {
        this.tag = tag;
        this.code.set(tag.getName());
        this.value.set(tag.getValue());
        this.description.set(tag.getDescription());
        this.visible.set(tag.getVisible());
    }

    public void updateVisibility(boolean state) {
        visible.setValue(state);
        tag.setVisible(state);
    }

    public String getCode() {
        return code.get();
    }

    public double getValue() {
        return value.get();
    }

    public String getDescription() {
        return description.get();
    }

    public boolean isVisible() {return visible.get();}
}
