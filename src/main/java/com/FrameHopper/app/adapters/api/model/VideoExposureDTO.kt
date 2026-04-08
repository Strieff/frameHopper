package com.FrameHopper.app.adapters.api.model

data class VideoExposureDTO(
    val id: Int,
    val name: String,
    val metadata: VideoMetadataExposureDTO,
    val frames: List<FrameExposureDTO>?,
    val notes: List<CommentExposureDTO>?,
    val uniqueTags: Int?,
    val totalPoints: Double?,
    val complexity: Double?
)

data class VideoMetadataExposureDTO(
    val totalFrames: Int,
    val frameRate: Double,
    val duration: Double,
)
