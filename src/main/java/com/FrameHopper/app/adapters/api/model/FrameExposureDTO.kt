package com.FrameHopper.app.adapters.api.model

data class FrameExposureDTO(
    val videoId: Int,
    val frameNumber: Int,
    val tags: List<TagExposureDTO>
)
