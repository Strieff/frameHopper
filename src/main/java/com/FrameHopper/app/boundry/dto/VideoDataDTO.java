package com.FrameHopper.app.boundry.dto;

import java.util.List;

public record VideoDataDTO(
        VideoDTO video,
        List<FrameDTO> frames
) {
}
