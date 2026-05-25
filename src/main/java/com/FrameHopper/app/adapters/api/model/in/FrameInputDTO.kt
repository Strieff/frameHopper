package com.FrameHopper.app.adapters.api.model.`in`

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Input model for a frame.")
data class FrameInputDTO(
    @field:Schema(
        description = "ID of the frame",
        example = "1",
    )
    val id: Int?,

    @field:Schema(
        description = "Number in sequence of the frame",
        example = "1",
    )
    val frameNo: Int,

    @field:Schema(
        description = "ID of a video the frame belongs to",
        example = "1",
    )
    val videoId: Int,

    @field:Schema(
        description = "Operation to perform on frame",
    )
    val operation: FrameOperation?,

    @field:Schema(
        description = "IDs of tags the operation should act upon",
    )
    val data: List<Int>?
)

@Schema(description = "Available operations.")
enum class FrameOperation {
    INSERT,
    DELETE
}
