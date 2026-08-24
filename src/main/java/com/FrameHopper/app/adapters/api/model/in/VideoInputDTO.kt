package com.FrameHopper.app.adapters.api.model.`in`

import com.FrameHopper.app.adapters.api.model.AvailableTagAnalytics
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

@Schema(description = "Input model for file analytics")
data class FileAnalyticsInputDTO (
    @field:Schema(
        description = "Language to export in. If empty default language is chosen",
        example = "en"
    )
    val language: String? = null,

    @field:Schema(
        description = "IDs of videos for analytics",
    )
    val videoIds: List<Int>,

    @field:Schema(
        description = "IDs of tags for analytics. If empty it is assumed all tags should be taken into account",
    )
    val tagIds: List<Int>?,

    @field:Schema(
        description = "Set of requested video analytics. If empty then it is assumed all analytics are requested",
    )
    val videoAnalytics: List<AvailableVideoAnalytics>?,

    @field:Schema(
        description = "Set of requested tag analytics. If empty then it is assumed all analytics are requested",
    )
    val tagAnalytics: List<AvailableTagAnalytics>?
)