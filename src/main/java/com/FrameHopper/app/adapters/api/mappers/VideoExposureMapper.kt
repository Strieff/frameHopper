package com.FrameHopper.app.adapters.api.mappers

import com.FrameHopper.app.adapters.api.model.VideoExposureDTO
import com.FrameHopper.app.adapters.api.model.VideoMetadataExposureDTO
import com.FrameHopper.app.boundry.dto.CommentDTO
import com.FrameHopper.app.boundry.dto.FrameDTO
import com.FrameHopper.app.boundry.dto.VideoDTO

object VideoExposureMapper {
    fun toExposure(
        video: VideoDTO,
        frames: List<FrameDTO>? = null,
        notes: List<CommentDTO>? = null,
        uniqueTags: Int? = null,
        totalPoints: Double? = null,
        complexity: Double? = null
    ): VideoExposureDTO {
        return VideoExposureDTO(
            id = video.id,
            name = video.name,
            metadata = VideoMetadataExposureDTO(
                totalFrames =  video.metadata.totalFrames,
                frameRate = video.metadata.frameRate,
                duration = video.metadata.duration
            ),
            frames = frames?.map(FrameExposureMapper::toExposure),
            notes = notes?.map(CommentExposureMapper::toExposure),
            uniqueTags = uniqueTags,
            totalPoints = totalPoints,
            complexity = complexity
        )
    }
}