package com.FrameHopper.app.boundry.dto.analytics;

import com.FrameHopper.app.boundry.dto.FrameDTO;
import com.FrameHopper.app.boundry.dto.VideoDTO;

import java.util.List;

public record VideoDataDTO(
        VideoDTO video,
        List<FrameDTO> frames
) {
}
