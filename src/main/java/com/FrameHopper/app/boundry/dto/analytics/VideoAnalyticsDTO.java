package com.FrameHopper.app.boundry.dto.analytics;

public record VideoAnalyticsDTO (
        String name,
        int frameCount,
        double framerate,
        double runtime,
        int uniqueTags,
        double totalPoints,
        double complexity
) { }
