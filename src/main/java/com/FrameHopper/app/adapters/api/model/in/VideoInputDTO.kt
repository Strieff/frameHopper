package com.FrameHopper.app.adapters.api.model.`in`

import com.FrameHopper.app.adapters.api.model.AvailableVideoAnalytics
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Input model for a video.")
data class VideoInputDTO (
    @field:Schema(
        description = "ID of the video",
        example = "1",
    )
    val id: Int?,

    @field:Schema(
        description = "Path of the video",
        example = "Video34.mp4",
    )
    val path: String
)

@Schema(description = "Input model for video analytics")
data class VideoAnalyticsInputDTO (
    @field:Schema(
        description = "IDs of videos for analytics",
    )
    val videoIds: List<Int>,

    @field:Schema(
        description = "Set of requested analytics. If empty then it is assumed all analytics are requested",
    )
    val analytics: List<AvailableVideoAnalytics>?
)