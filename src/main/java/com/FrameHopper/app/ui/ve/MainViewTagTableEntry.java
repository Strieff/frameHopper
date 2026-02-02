package com.FrameHopper.app.ui.ve;

import com.FrameHopper.app.core.domain.Tag;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;

public class MainViewTagTableEntry {
    Tag entry;

    @Getter
    private final SimpleStringProperty name;
    @Getter
    private final SimpleDoubleProperty value;

    public MainViewTagTableEntry(Tag entry) {
        this.entry = entry;

        this.name = new SimpleStringProperty(entry.getName());
        this.value = new SimpleDoubleProperty(entry.getValue());
    }

    public String getName() {
        return entry.getName();
    }

    public double getValue() {
        return entry.getValue();
    }
}
