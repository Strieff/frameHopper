package com.FrameHopper.app.boundry.dto.analytics;

import com.FrameHopper.app.boundry.dto.TagDTO;

public record TagAnalyticsParameterDTO(
        TagDTO tag,
        Number data
) {}
