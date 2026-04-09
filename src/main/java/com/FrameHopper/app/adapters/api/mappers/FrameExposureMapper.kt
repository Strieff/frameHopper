package com.FrameHopper.app.adapters.api.mappers

import com.FrameHopper.app.adapters.api.model.FrameExposureDTO
import com.FrameHopper.app.boundry.dto.FrameDTO

object FrameExposureMapper {
    fun toExposure(frame: FrameDTO): FrameExposureDTO = FrameExposureDTO(
        frame.video.id,
        frame.frameNumber,
        frame.tags.map(TagExposureMapper::toExposure)
    )
}