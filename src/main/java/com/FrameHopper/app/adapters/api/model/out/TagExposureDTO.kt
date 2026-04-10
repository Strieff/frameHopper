package com.FrameHopper.app.adapters.api.model.out

import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED

@Schema(description = "Output model for a tag")
data class TagExposureDTO(
    @field:Schema(
        description = "ID of the tag",
        example = "1",
    )
    val id: Int,
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
        requiredMode = NOT_REQUIRED
    )
    val description: String?,
    @field:Schema(
        description = "Status of the tag",
        example = "true",
    )
    val visible: Boolean,
    @field:Schema(
        description = "Analytics value of how many times the tag has been used on given video",
        example = "2",
        requiredMode = NOT_REQUIRED
    )
    val amountUsed: Int? = null,
    @field:Schema(
        description = "Analytics value of total sum of points of given tag on given video",
        example = "37.30",
        requiredMode = NOT_REQUIRED,
    )
    val totalPoints: Double? = null,
)