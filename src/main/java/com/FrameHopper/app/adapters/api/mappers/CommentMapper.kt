package com.FrameHopper.app.adapters.api.mappers

import com.FrameHopper.app.adapters.api.model.CommentExposureDTO
import com.FrameHopper.app.boundry.dto.CommentDTO

object CommentMapper {
    fun toExposure(comment: CommentDTO): CommentExposureDTO {
        return CommentExposureDTO(comment.id, comment.content)
    }
}