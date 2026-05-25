package com.FrameHopper.app.adapters.api.model.out

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Output model for a tag")
data class TagExposureDTO(
    @field:Schema(
        description = "ID of the tag",
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
    val visible: Boolean,

    @field:Schema(
        description = "Analytics value of how many times the tag has been used on given video",
        example = "2",
    )
    val amountUsed: Int?,

    @field:Schema(
        description = "Analytics value of total sum of points of given tag on given video",
        example = "37.30",
    )
    val totalPoints: Double?,
)

data class TagAnalyticsExposureDTO(
    @field:Schema(
        description = "Name of the tag",
        example = "Example tag",
    )
    val name: String,

    @field:Schema(
        description = "Value of the tag",
        example = "21.37",
    )
    val value: Double,

    @field:Schema(
        description = "Amount of times tag was used",
        example = "100",
    )
    val amountUsed: Int?,

    @field:Schema(
        description = "Sum of points from all occurrences of the tag",
        example = "200.32",
    )
    val totalPoints: Double?,
)

data class TagDataAnalyticsExposureDTO(
    @field:Schema(
        description = "Analytics of tags",
    )
    val analytics: List<TagAnalyticsExposureDTO>,

    @field:Schema(
        description = "Total tag amount",
        example = "10",
    )
    val tagAmount: Int,

    @field:Schema(
        description = "Sum of points from all of the tags",
        example = "200.32",
    )
    val totalPoints: Double?
)