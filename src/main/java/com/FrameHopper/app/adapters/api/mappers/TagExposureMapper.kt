package com.FrameHopper.app.adapters.api.mappers

import com.FrameHopper.app.adapters.api.model.AvailableTagAnalytics
import com.FrameHopper.app.adapters.api.model.`in`.TagInputDTO
import com.FrameHopper.app.adapters.api.model.out.TagAnalyticsExposureDTO
import com.FrameHopper.app.adapters.api.model.out.TagDataAnalyticsExposureDTO
import com.FrameHopper.app.adapters.api.model.out.TagExposureDTO
import com.FrameHopper.app.boundry.dto.TagDTO
import com.FrameHopper.app.boundry.dto.analytics.TagAnalyticsDTO
import com.FrameHopper.app.boundry.dto.analytics.TagDataAnalyticsDTO

object TagExposureMapper {
    fun TagDTO.toExposure(
        amountUsed: Int? = null,
        totalPoints: Double? = null,
    ): TagExposureDTO = TagExposureDTO(
        id,
        name,
        value,
        description?.ifEmpty { null },
        visible,
        amountUsed,
        totalPoints,
    )

    fun TagInputDTO.fromExposure(): TagDTO = TagDTO(
        id = this.id ?: -1,
        name = this.name,
        value = this.value,
        description = this.description,
    )

    fun TagDataAnalyticsDTO.toExposure(
        analyticTypes: List<AvailableTagAnalytics>
    ) = TagDataAnalyticsExposureDTO(
        analytics = tagAnalytics.map { it.toExposure(analyticTypes) },
        tagAmount = tagAmount,
        totalPoints = if (analyticTypes.contains(AvailableTagAnalytics.TOTAL_POINTS)) totalPoints else null
    )

    fun TagAnalyticsDTO.toExposure(
        analyticTypes: List<AvailableTagAnalytics>
    ) = TagAnalyticsExposureDTO(
        name = name,
        value = value,
        amountUsed = if (analyticTypes.contains(AvailableTagAnalytics.AMOUNT_USED)) amountUsed else null,
        totalPoints = if (analyticTypes.contains(AvailableTagAnalytics.TOTAL_POINTS)) totalPoints else null
    )
}