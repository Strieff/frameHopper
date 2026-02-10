package com.FrameHopper.app.boundry.dto.analytics;

import java.util.List;

public record VideoDataAnalyticsDTO(
        List<VideoAnalyticsDTO> videoAnalytics,
        double averageFrameCount,
        double averageFramerate,

        double asl
) {
}

