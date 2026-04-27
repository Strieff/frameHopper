package com.FrameHopper.app.adapters.api.model.`in`

import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED

data class TagInputDTO(
    @field:Schema(
        description = "ID of the video",
        example = "1",
    )
    val id: Int?,
    @field:Schema(
        description = "Name of the tag",
        example = "Tag",
    )
    val name: String,
    @field:Schema(
        description = "Value of the tag",
        example = "1.0",
    )
    val value: Double,
    @field:Schema(
        description = "Description of the tag",
        example = "This is a tag",
    )
    val description: String?,
    @field:Schema(
        description = "Status of the tag",
        example = "true",
    )
    val visible: Boolean?
)
