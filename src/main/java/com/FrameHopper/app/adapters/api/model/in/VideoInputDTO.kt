package com.FrameHopper.app.adapters.api.model.`in`

import io.swagger.v3.oas.annotations.media.Schema

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

data class VideoAnalyticsInputDTO (
    val videoIds: List<Int>,
)