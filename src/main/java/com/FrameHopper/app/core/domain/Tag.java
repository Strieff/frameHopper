package com.FrameHopper.app.core.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
public class Tag {
    private final int id;
    private String name;
    @Setter
    private double value;
    @Setter
    private String description;
    private boolean visible;

    public Tag(int id, String name, double value, String description, boolean visible) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.description = description;
        this.visible = visible;
    }

    public Tag(int id, String name, double value, boolean visible) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.visible = visible;
    }

    public void changeName(String newName) {
        if (newName == null || newName.isEmpty())
            throw new IllegalArgumentException("New name cannot be empty");
        this.name = newName;
    }

    public void changeState() {
        this.visible ^= this.visible;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Tag tag)) return false;
        return id == tag.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
