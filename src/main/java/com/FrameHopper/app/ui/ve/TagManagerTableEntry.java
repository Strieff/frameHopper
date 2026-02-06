package com.FrameHopper.app.ui.ve;

import com.FrameHopper.app.boundry.dto.TagDTO;
import com.FrameHopper.app.core.domain.Tag;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;

public class TagManagerTableEntry {
    @Getter
    private final TagDTO tag;

    private final StringProperty code;
    private final DoubleProperty value;
    private final StringProperty description;

    public TagManagerTableEntry(TagDTO tag) {
        this.tag = tag;
        this.code = new SimpleStringProperty(tag.getName());
        this.value = new SimpleDoubleProperty(tag.getValue());
        this.description = new SimpleStringProperty(tag.getDescription());
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
