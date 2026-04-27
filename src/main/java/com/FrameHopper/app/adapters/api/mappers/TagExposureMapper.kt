package com.FrameHopper.app.adapters.api.mappers

import com.FrameHopper.app.adapters.api.model.`in`.TagInputDTO
import com.FrameHopper.app.adapters.api.model.out.TagExposureDTO
import com.FrameHopper.app.boundry.dto.TagDTO

object TagExposureMapper {
    fun toExposure(
        tag: TagDTO,
        amountUsed: Int? = null,
        totalPoints: Double? = null,
    ): TagExposureDTO = TagExposureDTO(
        tag.id,
        tag.name,
        tag.value,
        tag.description?.ifEmpty { null },
        tag.visible,
        amountUsed,
        totalPoints,
    )

    fun TagInputDTO.fromExposure(): TagDTO = TagDTO(
        id = this.id ?: -1,
        name = this.name,
        value = this.value,
        description = this.description,
    )
}