package com.FrameHopper.app.boundry.dto;

import java.util.List;
import java.util.Objects;

public record FrameDTO(
        int id,
        int frameNumber,
        VideoDTO video,
        List<TagDTO> tags
) {
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FrameDTO frameDTO)) return false;
        return id == frameDTO.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
