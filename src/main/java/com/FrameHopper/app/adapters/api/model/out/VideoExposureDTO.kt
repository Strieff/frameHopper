package com.FrameHopper.app.adapters.api.model.out

import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED

@Schema(description = "Output model of a video")
data class VideoExposureDTO(
    @field:Schema(
        description = "ID of the video",
        example = "1",
    )
    val id: Int,
    @field:Schema(
        description = "Name of the video",
        example = "Video34.mp4",
    )
    val name: String,
    @field:Schema(description = "Metadata of the video")
    val metadata: VideoMetadataExposureDTO,
    @field:Schema(
        description = "Frames of the video",
        requiredMode = NOT_REQUIRED
    )
    val frames: List<FrameExposureDTO>?,
    @field:Schema(
        description = "Comments on the video",
        requiredMode = NOT_REQUIRED
    )
    val notes: List<CommentExposureDTO>?,
    @field:Schema(
        description = "Analytics value of how many unique tags are used on the video",
        example = "7",
        requiredMode = NOT_REQUIRED
    )
    val uniqueTags: Int?,
    @field:Schema(
        description = "Analytics value of the total sum of all values of tags used on the video",
        example = "3250.5",
        requiredMode = NOT_REQUIRED
    )
    val totalPoints: Double?,
    @field:Schema(
        description = "Analytics value of total calculated complexity of the video",
        example = "3345.23",
        requiredMode = NOT_REQUIRED
    )
    val complexity: Double?
)

@Schema(description = "Output model of metadata of a video")
data class VideoMetadataExposureDTO(
    @field:Schema(
        description = "Total frames of the video",
        example = "350",
    )
    val totalFrames: Int,
    @field:Schema(
        description = "Framerate of the video",
        example = "24.0",
    )
    val frameRate: Double,
    @field:Schema(
        description = "Duration, in seconds, of the video",
        example = "3.58",
    )
    val duration: Double,
)
