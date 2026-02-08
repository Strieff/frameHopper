package com.FrameHopper.app.boundry.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
public class TagDTO {
    final int id;
    @Setter
    String name;
    @Setter
    Double value;
    @Setter
    String description;
    @Setter
    boolean visible;

    public TagDTO(int id, String name, Double value, String description, boolean visible) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.description = description;
        this.visible = visible;
    }

    public TagDTO(String name, Double value, String description) {
        id = -1;
        this.name = name;
        this.value = value;
        this.description = description;
        this.visible = true;
    }

    public void changeStatus() {
        visible = !visible;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TagDTO tagDTO)) return false;
        return id == tagDTO.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
