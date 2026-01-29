package com.FrameHopper.app.API.Model.Tag;

import com.FrameHopper.app.Model.Tag;

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
}
