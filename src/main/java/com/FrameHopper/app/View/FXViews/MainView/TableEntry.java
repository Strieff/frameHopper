package com.FrameHopper.app.View.FXViews.MainView;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class TableEntry {
    private final SimpleStringProperty name;
    private final SimpleDoubleProperty value;

    public TableEntry(String name, Double value) {
        this.name = new SimpleStringProperty(name);
        this.value = new SimpleDoubleProperty(value);
    }

    public String getName() {
        return name.get();
    }

    public double getValue() {
        return value.get();
    }
}
