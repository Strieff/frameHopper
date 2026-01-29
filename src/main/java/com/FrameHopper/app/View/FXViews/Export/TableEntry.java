package com.FrameHopper.app.View.FXViews.Export;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;
import lombok.Setter;

public class TableEntry {
    @Getter
    int id;
    @Getter
    @Setter
    private boolean hasListener = false;

    SimpleStringProperty name;
    SimpleBooleanProperty selected;

    public TableEntry(int id,String name) {
        this.id = id;
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
