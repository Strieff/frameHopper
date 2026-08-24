package com.FrameHopper.app.boundry.dto.analytics;

import com.FrameHopper.app.boundry.dto.VideoDTO;

public record VideoAnalyticsParameterDTO(
        VideoDTO video,
        Number data
) {
}
