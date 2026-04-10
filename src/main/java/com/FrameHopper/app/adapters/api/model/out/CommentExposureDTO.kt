package com.FrameHopper.app.adapters.api.model.out

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Output model for a comment on video.")
data class CommentExposureDTO(
    @field:Schema(
        description = "ID of the comment",
        example = "1",
    )
    val id: Int,
    @field:Schema(
        description = "Content of the comment",
        example = "This is a nice comment!",
    )
    val content: String
)
