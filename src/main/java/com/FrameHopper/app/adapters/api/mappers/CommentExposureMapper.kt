package com.FrameHopper.app.adapters.api.mappers

import com.FrameHopper.app.adapters.api.model.out.CommentExposureDTO
import com.FrameHopper.app.boundry.dto.CommentDTO

object CommentExposureMapper {
    fun toExposure(comment: CommentDTO): CommentExposureDTO = CommentExposureDTO(comment.id, comment.content)
}