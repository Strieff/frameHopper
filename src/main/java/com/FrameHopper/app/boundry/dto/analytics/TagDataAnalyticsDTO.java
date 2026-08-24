package com.FrameHopper.app.boundry.dto.analytics;

import java.util.List;

public record TagDataAnalyticsDTO(
        List<TagAnalyticsDTO> tagAnalytics,
        int tagAmount,
        double totalPoints
) {
}
