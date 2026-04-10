package com.FrameHopper.app.adapters.api.exposure

import com.FrameHopper.app.adapters.api.mappers.TagExposureMapper
import com.FrameHopper.app.adapters.api.model.out.TagExposureDTO
import com.FrameHopper.app.boundry.dto.analytics.VideoDataDTO
import com.FrameHopper.app.core.application.analytics.TagAnalyticsQuery
import com.FrameHopper.app.core.ports.`in`.frame.FrameQuery
import com.FrameHopper.app.core.ports.`in`.tag.TagsQuery
import com.FrameHopper.app.core.ports.`in`.video.VideoQuery
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/tags")
open class TagController(
    val tagsQuery: TagsQuery,
    val videoQuery: VideoQuery,
    val frameQuery: FrameQuery,
    val tagAnalyticsQuery: TagAnalyticsQuery
) {
    @Operation(summary = "Get all tags")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Tags found",
            )
        ]
    )
    @GetMapping
    fun getTags(): ResponseEntity<List<TagExposureDTO>> {
        val tags = tagsQuery.getAllTags() ?: return ResponseEntity.ok().build()
        val exposureData = tags.map(TagExposureMapper::toExposure)

        return ResponseEntity.ok(exposureData)
    }

    @Operation(summary = "Get tag by id")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Tag found",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Tag not found",
            )
        ]
    )
    @GetMapping("id/{tagId}")
    fun getTagById(
        @Parameter(description = "ID of the tag", example = "5")
        @PathVariable tagId: Int
    ): ResponseEntity<TagExposureDTO> {
        val tag = tagsQuery.getTagById(tagId) ?: return ResponseEntity.notFound().build()
        val exposureData = TagExposureMapper.toExposure(tag)

        return ResponseEntity.ok(exposureData)
    }

    @Operation(summary = "Get tag by name")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Tag found",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Tag not found",
            )
        ]
    )
    @GetMapping("name/{name}")
    fun getTagByName(
        @Parameter(description = "Name of the video", example = "Tag1")
        @PathVariable name: String
    ): ResponseEntity<TagExposureDTO> {
        val tag = tagsQuery.getTagByName(name) ?: return ResponseEntity.notFound().build()
        val exposureData = TagExposureMapper.toExposure(tag)

        return ResponseEntity.ok(exposureData)
    }

    @Operation(summary = "Get all tags on video by video id")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Tags found",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Video not found",
            )
        ]
    )
    @GetMapping("videoId/{videoId}")
    fun getTagsByVideoId(
        @Parameter(description = "ID of the video", example = "5")
        @PathVariable videoId: Int
    ): ResponseEntity<List<TagExposureDTO>> {
        val video = videoQuery.getVideoById(videoId) ?: return ResponseEntity.notFound().build()
        val tags = tagsQuery.getAllOnVideo(video) ?: return ResponseEntity.ok().build()
        val frames = frameQuery.getAllFramesOnVideo(video) ?: mutableListOf()

        val exposureData = tags.map { TagExposureMapper.toExposure(
            tag = it,
            amountUsed = tagAnalyticsQuery.getAmountUsed(it, listOf(VideoDataDTO(video, frames))).data.toInt(),
            totalPoints = tagAnalyticsQuery.getTotalPoints(it, listOf(VideoDataDTO(video, frames))).data.toDouble()
        ) }

        return ResponseEntity.ok(exposureData)
    }
}