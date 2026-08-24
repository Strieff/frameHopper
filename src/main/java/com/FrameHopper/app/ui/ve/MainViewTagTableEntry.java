package com.FrameHopper.app.ui.ve;

import com.FrameHopper.app.boundry.dto.TagDTO;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;

@Getter
public class MainViewTagTableEntry {
    private final TagDTO entry;

    private final SimpleStringProperty name;
    private final SimpleDoubleProperty value;

    public MainViewTagTableEntry(TagDTO entry) {
        this.entry = entry;

        this.name = new SimpleStringProperty(entry.getName());
        this.value = new SimpleDoubleProperty(entry.getValue());
    }

    public String getName() {
        return name.get();
    }

    public double getValue() {
        return value.get();
    }
}
