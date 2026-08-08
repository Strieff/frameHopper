package com.FrameHopper.app.adapters.api.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Available tag analytics")
enum class AvailableTagAnalytics {
    VALUE,
    AMOUNT_USED,
    TOTAL_POINTS
}

@Schema(description = "Available video analytics")
enum class AvailableVideoAnalytics {
    FRAME_AMOUNT,
    FRAMERATE,
    DURATION,
    CODES,
    POINTS,
    COMPLEXITY,
    ASL
}