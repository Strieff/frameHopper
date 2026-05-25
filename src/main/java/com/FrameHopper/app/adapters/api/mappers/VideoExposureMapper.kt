package com.FrameHopper.app.adapters.api.mappers

import com.FrameHopper.app.adapters.api.mappers.CommentExposureMapper.toExposure
import com.FrameHopper.app.adapters.api.mappers.FrameExposureMapper.toExposure
import com.FrameHopper.app.adapters.api.model.AvailableVideoAnalytics
import com.FrameHopper.app.adapters.api.model.out.VideoAnalyticsExposureDTO
import com.FrameHopper.app.adapters.api.model.out.VideoDataAnalyticsExposureDTO
import com.FrameHopper.app.adapters.api.model.out.VideoExposureDTO
import com.FrameHopper.app.adapters.api.model.out.VideoMetadataExposureDTO
import com.FrameHopper.app.boundry.dto.CommentDTO
import com.FrameHopper.app.boundry.dto.FrameDTO
import com.FrameHopper.app.boundry.dto.VideoDTO
import com.FrameHopper.app.boundry.dto.analytics.VideoAnalyticsDTO
import com.FrameHopper.app.boundry.dto.analytics.VideoDataAnalyticsDTO

object VideoExposureMapper {
    fun VideoDTO.toExposure(
        frames: List<FrameDTO>? = null,
        notes: List<CommentDTO>? = null,
        uniqueTags: Int? = null,
        totalPoints: Double? = null,
        complexity: Double? = null
    ): VideoExposureDTO = VideoExposureDTO(
        id = id,
        name = name,
        metadata = VideoMetadataExposureDTO(
            totalFrames = metadata.totalFrames,
            frameRate = metadata.frameRate,
            duration = metadata.duration
        ),
        frames = frames?.map {it.toExposure()},
        notes = notes?.map {it.toExposure()},
        uniqueTags = uniqueTags,
        totalPoints = totalPoints,
        complexity = complexity
    )

    fun VideoDataAnalyticsDTO.toExposure(
        analyticTypes: List<AvailableVideoAnalytics>
    ) = VideoDataAnalyticsExposureDTO(
        analytics = videoAnalytics.map { it.toExposure(analyticTypes) },
        totalShotAmount = videoAnalytics.size,
        totalFrameAmount = if (analyticTypes.contains(AvailableVideoAnalytics.FRAME_AMOUNT)) totalFrameAmount else null,
        averageFrameAmount = if (analyticTypes.contains(AvailableVideoAnalytics.FRAME_AMOUNT)) averageFrameAmount else null,
        averageFramerate = if (analyticTypes.contains(AvailableVideoAnalytics.FRAMERATE)) averageFramerate else null,
        totalDuration = if (analyticTypes.contains(AvailableVideoAnalytics.DURATION)) totalRuntime else null,
        averageDuration = if (analyticTypes.contains(AvailableVideoAnalytics.DURATION)) totalRuntime else null,
        averageCodeAmount = if (analyticTypes.contains(AvailableVideoAnalytics.CODES)) averageCodeAmount else null,
        totalPoints = if (analyticTypes.contains(AvailableVideoAnalytics.POINTS)) totalPoints else null,
        averagePoints = if (analyticTypes.contains(AvailableVideoAnalytics.POINTS)) averagePoints else null,
        overallComplexity = if (analyticTypes.contains(AvailableVideoAnalytics.COMPLEXITY)) overallComplexity else null,
        averageComplexity = if (analyticTypes.contains(AvailableVideoAnalytics.COMPLEXITY)) averageComplexity else null,
        asl = if (analyticTypes.contains(AvailableVideoAnalytics.ASL)) asl else null,
    )

    fun VideoAnalyticsDTO.toExposure(
        analyticTypes: List<AvailableVideoAnalytics>
    ) = VideoAnalyticsExposureDTO(
        name = name,
        frameAmount = if (analyticTypes.contains(AvailableVideoAnalytics.FRAME_AMOUNT)) frameCount else null,
        frameRate = if (analyticTypes.contains(AvailableVideoAnalytics.FRAME_AMOUNT)) framerate else null,
        duration = if (analyticTypes.contains(AvailableVideoAnalytics.DURATION)) runtime else null,
        uniqueCodes = if (analyticTypes.contains(AvailableVideoAnalytics.CODES)) uniqueTags else null,
        totalPoints = if (analyticTypes.contains(AvailableVideoAnalytics.POINTS)) totalPoints else null,
        complexity = if (analyticTypes.contains(AvailableVideoAnalytics.COMPLEXITY)) complexity else null
    )
}