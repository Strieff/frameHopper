package com.FrameHopper.app.adapters.api.mappers

import com.FrameHopper.app.adapters.api.mappers.TagExposureMapper.toExposure
import com.FrameHopper.app.adapters.api.model.`in`.FrameInputDTO
import com.FrameHopper.app.adapters.api.model.out.FrameExposureDTO
import com.FrameHopper.app.boundry.dto.FrameDTO
import com.FrameHopper.app.boundry.dto.TagDTO
import com.FrameHopper.app.boundry.dto.VideoDTO

object FrameExposureMapper {
    fun FrameDTO.toExposure(): FrameExposureDTO = FrameExposureDTO(
        video.id,
        frameNumber,
        tags.map {it.toExposure()}
    )

    fun FrameInputDTO.fromExposure(video: VideoDTO, tags: List<TagDTO>?): FrameDTO = FrameDTO(
        id ?: -1,
        frameNo,
        video,
        tags ?: emptyList(),
    )
}