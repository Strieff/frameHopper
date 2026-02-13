package com.FrameHopper.app.boundry.dto.analytics;

import java.util.List;

public record VideoDataAnalyticsDTO(
        List<VideoAnalyticsDTO> videoAnalytics,
        int totalShotAmount,

        int totalFrameAmount,
        double averageFrameAmount,

        double averageCodeAmount,

        double totalRuntime,
        double averageRuntime,

        double averageFramerate,

        double totalPoints,
        double averagePoints,

        double overallComplexity,
        double averageComplexity,

        double asl
) {
}

