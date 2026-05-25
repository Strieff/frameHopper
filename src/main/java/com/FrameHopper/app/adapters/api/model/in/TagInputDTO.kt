package com.FrameHopper.app.adapters.api.model.`in`

import com.FrameHopper.app.adapters.api.model.AvailableTagAnalytics
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Input model for a tag.")
data class TagInputDTO(
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
    val visible: Boolean?
)

@Schema(description = "Input model for tag analytics")
data class TagAnalyticsInputDTO(
    @field:Schema(
        description = "IDs of tags for analytics",
    )
    val tagIds: List<Int>,

    @field:Schema(
        description = "IDs of videos for analytics",
    )
    val videoIds: List<Int>,

    @field:Schema(
        description = "Set of requested analytics. If empty then it is assumed all analytics are requested",
    )
    val analytics: List<AvailableTagAnalytics>?
)
