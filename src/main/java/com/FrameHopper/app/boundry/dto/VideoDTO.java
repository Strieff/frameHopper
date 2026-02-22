package com.FrameHopper.app.boundry.dto;

import java.util.ArrayList;
import java.util.Objects;

public record VideoDTO(
        int id,
        String name,
        String path,
        MetadataDto metadata,
        ArrayList<CommentDTO> comments
) {
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VideoDTO videoDTO)) return false;
        return id == videoDTO.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
