package com.FrameHopper.app.ui.ve;

import com.FrameHopper.app.boundry.dto.TagDTO;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;

public class TagManagerTableEntry {
    @Getter
    private TagDTO tag;

    private final StringProperty code;
    private final DoubleProperty value;
    private final StringProperty description;

    public TagManagerTableEntry(TagDTO tag) {
        this.tag = tag;
        this.code = new SimpleStringProperty(tag.getName());
        this.value = new SimpleDoubleProperty(tag.getValue());
        this.description = new SimpleStringProperty(tag.getDescription());
    }

    public void setTag(TagDTO tag) {
        this.tag = tag;
        this.code.set(tag.getName());
        this.value.set(tag.getValue());
        this.description.set(tag.getDescription());
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
}
