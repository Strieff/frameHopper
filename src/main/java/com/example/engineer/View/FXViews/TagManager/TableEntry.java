package com.example.engineer.View.FXViews.TagManager;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;

public class TableEntry {
    private final BooleanProperty selected;
    private final SimpleStringProperty name;
    private final SimpleDoubleProperty value;

    @Getter
    private final int id;

    public TableEntry(int id,Boolean selected,String name, Double value) {
        this.id = id;
        this.selected = new SimpleBooleanProperty(selected);
        this.name = new SimpleStringProperty(name);
        this.value = new SimpleDoubleProperty(value);
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
}
