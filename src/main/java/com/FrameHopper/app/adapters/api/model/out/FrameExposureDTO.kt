package com.FrameHopper.app.adapters.api.model.out

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Output model for a frame of a video.")
data class FrameExposureDTO(
    @field:Schema(
        description = "ID of the frame",
        example = "1",
    )
    val videoId: Int,
    @field:Schema(
        description = "Number of the frame in sequence",
        example = "1",
    )
    val frameNumber: Int,
    @field:Schema(description = "Tags on the frame")
    val tags: List<TagExposureDTO>
)
