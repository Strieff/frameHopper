package com.FrameHopper.app.boundry.dto.analytics;

public record TagAnalyticsDTO(
    String name,
    double value,
    int amountUsed,
    double totalPoints
) { }
