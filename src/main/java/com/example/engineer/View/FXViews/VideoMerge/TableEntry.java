package com.example.engineer.View.FXViews.VideoMerge;

import javafx.beans.property.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class TableEntry {
    private final BooleanProperty selected;
    private final SimpleIntegerProperty number;
    private final SimpleIntegerProperty amount;

    @Getter
    private final int id;
    @Getter
    @Setter
    private boolean hasListener = false;
    @Getter
    private List<String> tags;
    @Getter
    @Setter
    boolean selectable = true;

    public TableEntry(int id,Boolean selected,Integer number, Integer amount, List<String> tags) {
        this.id = id;
        this.selected = new SimpleBooleanProperty(selected);
        this.number = new SimpleIntegerProperty(number);
        this.amount = new SimpleIntegerProperty(amount);
        this.tags = tags;
    }

    public int getNumber() {
        return number.get();
    }

    public int getAmount() {
        return amount.get();
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }
}
