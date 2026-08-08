package com.FrameHopper.app.adapters.api.mappers

import com.FrameHopper.app.adapters.api.model.AvailableTagAnalytics
import com.FrameHopper.app.adapters.api.model.AvailableVideoAnalytics
import com.FrameHopper.app.boundry.dto.export.ChosenTagAnalytics
import com.FrameHopper.app.boundry.dto.export.ChosenVideoAnalytics

object AnalyticsMapper {
    fun AvailableVideoAnalytics.toExport(): ChosenVideoAnalytics? = when (this) {
        AvailableVideoAnalytics.FRAME_AMOUNT -> ChosenVideoAnalytics.FRAME_COUNT
        AvailableVideoAnalytics.FRAMERATE -> ChosenVideoAnalytics.FRAMERATE
        AvailableVideoAnalytics.DURATION -> ChosenVideoAnalytics.RUNTIME
        AvailableVideoAnalytics.CODES -> ChosenVideoAnalytics.UNIQUE_TAGS
        AvailableVideoAnalytics.POINTS -> ChosenVideoAnalytics.TOTAL_POINTS
        AvailableVideoAnalytics.COMPLEXITY -> ChosenVideoAnalytics.COMPLEXITY
        AvailableVideoAnalytics.ASL -> null
    }

    fun AvailableTagAnalytics.toExport(): ChosenTagAnalytics = when (this) {
        AvailableTagAnalytics.VALUE -> ChosenTagAnalytics.VALUE
        AvailableTagAnalytics.AMOUNT_USED -> ChosenTagAnalytics.AMOUNT
        AvailableTagAnalytics.TOTAL_POINTS -> ChosenTagAnalytics.TOTAL_POINTS
    }
}