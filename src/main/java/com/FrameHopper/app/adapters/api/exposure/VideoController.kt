package com.FrameHopper.app.adapters.api.exposure

import com.FrameHopper.app.adapters.api.mappers.VideoExposureMapper
import com.FrameHopper.app.adapters.api.model.out.VideoExposureDTO
import com.FrameHopper.app.boundry.dto.analytics.VideoDataDTO
import com.FrameHopper.app.core.application.analytics.VideoAnalyticsQuery
import com.FrameHopper.app.core.ports.`in`.comment.CommentsQuery
import com.FrameHopper.app.core.ports.`in`.frame.FrameQuery
import com.FrameHopper.app.core.ports.`in`.video.VideoQuery
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
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
    @Operation(summary = "Get all videos")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Videos found",
            )
        ]
    )
    @GetMapping
    fun getVideos(): ResponseEntity<List<VideoExposureDTO>> {
        val videos = videoQuery.allVideos
        if(videos.isNullOrEmpty()) return ResponseEntity.ok().build()
        val exposureData = videos.map(VideoExposureMapper::toExposure)

        return ResponseEntity.ok(exposureData)
    }


    @Operation(summary = "Get video by id")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Video found",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Video not found",
            )
        ]
    )
    @GetMapping("/id/{videoId}")
    fun getVideoById(
        @Parameter(description = "ID of the video", example = "5")
        @PathVariable videoId: Int,

        @Parameter(description = "Indicate whether to show analytics", example = "false")
        @RequestParam(required = false, defaultValue = "false") showAnalytics: Boolean,

        @Parameter(description = "Indicate whether to show frames", example = "true")
        @RequestParam(required = false, defaultValue = "true") showFrames: Boolean,

        @Parameter(description = "Indicate whether to show notes", example = "false")
        @RequestParam(required = false, defaultValue = "false") showNotes: Boolean,
    ): ResponseEntity<VideoExposureDTO> {
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

        return ResponseEntity.ok(exposureData)
    }

    @Operation(summary = "Get videos by name")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Videos found",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Videos not found",
            )
        ]
    )
    @GetMapping("/name/{videoName}")
    fun getVideoById(
        @Parameter(description = "Name of the video", example = "Video.mp4")
        @PathVariable videoName: String,

        @Parameter(description = "Indicate whether to show analytics", example = "false")
        @RequestParam(required = false, defaultValue = "false") showAnalytics: Boolean,

        @Parameter(description = "Indicate whether to show frames", example = "true")
        @RequestParam(required = false, defaultValue = "true") showFrames: Boolean,

        @Parameter(description = "Indicate whether to show notes", example = "false")
        @RequestParam(required = false, defaultValue = "false") showNotes: Boolean,
    ): ResponseEntity<List<VideoExposureDTO>> {
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

        return ResponseEntity.ok(exposureData)
    }
}