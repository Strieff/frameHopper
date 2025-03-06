package com.example.engineer.API.Model.Tag;

import com.example.engineer.Model.Tag;
import com.google.gson.GsonBuilder;

public record SimpleTagDTO(
        int id,
        String name,
        double value,
        String description,
        boolean hidden
) {
    public SimpleTagDTO(Tag tag) {
        this(
                tag.getId(),
                tag.getName(),
                tag.getValue(),
                tag.getDescription(),
                tag.isDeleted()
        );
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
}
