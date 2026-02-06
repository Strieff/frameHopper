package com.FrameHopper.app.boundry.dto;

import java.util.List;

public record FrameDTO(
        int id,
        int frameNumber,
        VideoDTO video,
        List<TagDTO> tags
) {
}
