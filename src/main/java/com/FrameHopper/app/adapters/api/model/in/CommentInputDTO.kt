package com.FrameHopper.app.adapters.api.model.`in`

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Input model for a comment.")
data class CommentInputDTO(
    @field:Schema(
        description = "ID of the comment",
        example = "1",
    )
    val id: Int?,

    @field:Schema(
        description = "Content of the comment",
        example = "Example comment content",
    )
    val content: String,

    @field:Schema(
        description = "Listing order of the comment",
        example = "4",
    )
    val listingOrder: Int?,

    @field:Schema(
        description = "Id of the video the comment is about",
        example = "1",
    )
    val videoId: Int
)
