package com.FrameHopper.app.View.FXViews.TagManager;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;

public class TableEntry {
    @Getter
    private final int id;
    private final StringProperty code;
    private final DoubleProperty value;
    private final StringProperty description;

    public TableEntry(int id, String code, double value, String description) {
        this.id = id;
        this.code = new SimpleStringProperty(code);
        this.value = new SimpleDoubleProperty(value);
        this.description = new SimpleStringProperty(description);
    }

    public String getCode() {
        return code.get();
    }

    public void setCode(String name){
        code.set(name);
    }

    public double getValue() {
        return value.get();
    }

    public void setValue(double value){
        this.value.set(value);
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description){
        this.description.set(description);
    }
}
