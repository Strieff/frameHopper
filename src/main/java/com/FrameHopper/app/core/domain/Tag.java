package com.FrameHopper.app.core.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Tag {
    private final int id;
    private String name;
    @Setter
    private double value;
    @Setter
    private String description;
    private boolean visible;

    public Tag(int id, String name, double value, String description,  boolean visible) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.description = description;
    }

    public Tag(int id, String name, double value, boolean visible) {
        this.id = id;
        this.name = name;
        this.value = value;
    }

    public void changeName(String newName) {
        if (newName == null || newName.isEmpty())
            throw new IllegalArgumentException("New name cannot be empty");
        this.name = newName;
    }

    public void changeState() {
        this.visible ^= this.visible;
    }
}
