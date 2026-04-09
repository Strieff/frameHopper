package com.FrameHopper.app.adapters.api.mappers

import com.FrameHopper.app.adapters.api.model.TagExposureDTO
import com.FrameHopper.app.boundry.dto.TagDTO

object TagExposureMapper {
    fun toExposure(
        tag: TagDTO,
        amountUsed: Int? = null,
        totalPoints: Double? = null,
    ): TagExposureDTO = TagExposureDTO(
        tag.id,
        tag.name,
        tag.value ?: 0.0,
        tag.description.ifEmpty { null },
        tag.visible,
        amountUsed,
        totalPoints,
    )
}