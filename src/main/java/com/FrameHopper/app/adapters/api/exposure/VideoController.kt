package com.FrameHopper.app.adapters.api.exposure

import com.FrameHopper.app.adapters.api.mappers.VideoExposureMapper
import com.FrameHopper.app.boundry.dto.analytics.VideoDataDTO
import com.FrameHopper.app.core.application.analytics.VideoAnalyticsQuery
import com.FrameHopper.app.core.ports.`in`.comment.CommentsQuery
import com.FrameHopper.app.core.ports.`in`.frame.FrameQuery
import com.FrameHopper.app.core.ports.`in`.video.VideoQuery
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/videos")
class VideoController(
    val frameQuery: FrameQuery,
    val videoQuery: VideoQuery,
    val commentsQuery: CommentsQuery,
    val videoAnalyticsQuery: VideoAnalyticsQuery
) {
    @GetMapping()
    fun getVideos(): ResponseEntity<String> {
        val videos = videoQuery.allVideos
        if(videos.isNullOrEmpty()) return ResponseEntity.noContent().build()

        val exposureData = videos.map(VideoExposureMapper::toExposure)
        return ResponseEntity<String>(ObjectMapper().setSerializationInclusion(Include.NON_NULL).writeValueAsString(exposureData), HttpStatus.OK)
    }


    @GetMapping("/id/{videoId}")
    fun getVideoById(
        @PathVariable videoId: Int,
        @RequestParam(required = false, defaultValue = "false") showAnalytics: Boolean,
        @RequestParam(required = false, defaultValue = "true") showFrames: Boolean,
        @RequestParam(required = false, defaultValue = "false") showNotes: Boolean,
    ): ResponseEntity<String> {
        val video = videoQuery.getVideoById(videoId) ?: return ResponseEntity.notFound().build()
        val frames = if(showFrames) frameQuery.getAllFramesOnVideo(video) else null
        val comments = if(showNotes) commentsQuery.getAllCommentsByVideo(video) else null

        val uniqueTags = if (showAnalytics) videoAnalyticsQuery.getUniqueTagsCount(VideoDataDTO(video, frames)).data.toInt() else null
        val totalPoints = if(showAnalytics) videoAnalyticsQuery.getTotalPoints(VideoDataDTO(video, frames)).data.toDouble() else null
        val complexity = if(showAnalytics) videoAnalyticsQuery.getComplexity(VideoDataDTO(video, frames)).data.toDouble() else null

        val exposureData = VideoExposureMapper.toExposure(
            video = video,
            frames = frames,
            notes = comments,
            uniqueTags = uniqueTags,
            totalPoints = totalPoints,
            complexity = complexity,
        )

        return ResponseEntity<String>(ObjectMapper().setSerializationInclusion(Include.NON_NULL).writeValueAsString(exposureData), HttpStatus.OK)
    }

    @GetMapping("/name/{videoName}")
    fun getVideoById(
        @PathVariable videoName: String,
        @RequestParam(required = false, defaultValue = "false") showAnalytics: Boolean,
        @RequestParam(required = false, defaultValue = "true") showFrames: Boolean,
        @RequestParam(required = false, defaultValue = "false") showNotes: Boolean,
    ): ResponseEntity<String> {
        val videos = videoQuery.getVideoByName(videoName) ?: return ResponseEntity.notFound().build()
        val exposureData = videos.map {
            val frames = if(showFrames) frameQuery.getAllFramesOnVideo(it) else null
            val comments = if(showNotes) commentsQuery.getAllCommentsByVideo(it) else null

            val uniqueTags = if (showAnalytics) videoAnalyticsQuery.getUniqueTagsCount(VideoDataDTO(it, frames)).data.toInt() else null
            val totalPoints = if(showAnalytics) videoAnalyticsQuery.getTotalPoints(VideoDataDTO(it, frames)).data.toDouble() else null
            val complexity = if(showAnalytics) videoAnalyticsQuery.getComplexity(VideoDataDTO(it, frames)).data.toDouble() else null

            VideoExposureMapper.toExposure(
                video = it,
                frames = frames,
                notes = comments,
                uniqueTags = uniqueTags,
                totalPoints = totalPoints,
                complexity = complexity,
            )
        }

        return ResponseEntity<String>(ObjectMapper().setSerializationInclusion(Include.NON_NULL).writeValueAsString(exposureData), HttpStatus.OK)
    }
}