package com.FrameHopper.app.adapters.api.mappers

import com.FrameHopper.app.adapters.api.model.`in`.CommentInputDTO
import com.FrameHopper.app.adapters.api.model.out.CommentExposureDTO
import com.FrameHopper.app.boundry.dto.CommentDTO

object CommentExposureMapper {
    fun CommentDTO.toExposure(): CommentExposureDTO = CommentExposureDTO(id, content)

    fun CommentInputDTO.fromExposure(listingOrder: Int? = 0): CommentDTO = CommentDTO(
        id ?: -1,
        content,
        listingOrder ?: 0,
        videoId
    )
}