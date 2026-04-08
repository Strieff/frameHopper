package com.FrameHopper.app.adapters.api.exposure

import com.FrameHopper.app.adapters.api.mappers.TagExposureMapper
import com.FrameHopper.app.boundry.dto.analytics.VideoDataDTO
import com.FrameHopper.app.core.application.analytics.TagAnalyticsQuery
import com.FrameHopper.app.core.ports.`in`.frame.FrameQuery
import com.FrameHopper.app.core.ports.`in`.tag.TagsQuery
import com.FrameHopper.app.core.ports.`in`.video.VideoQuery
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
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
    @GetMapping
    fun getTags(): ResponseEntity<String> {
        val tags = tagsQuery.getAllTags() ?: return ResponseEntity(HttpStatus.NO_CONTENT)
        val exposureData = tags.map(TagExposureMapper::toExposure)

        return ResponseEntity<String>(ObjectMapper().setSerializationInclusion(Include.NON_NULL).writeValueAsString(exposureData), HttpStatus.OK)
    }

    @GetMapping("id/{tagId}")
    fun getTagById(@PathVariable tagId: Int): ResponseEntity<String> {
        val tag = tagsQuery.getTagById(tagId) ?: return ResponseEntity.notFound().build()
        val exposureData = TagExposureMapper.toExposure(tag)

        return ResponseEntity<String>(ObjectMapper().setSerializationInclusion(Include.NON_NULL).writeValueAsString(exposureData), HttpStatus.OK)
    }

    @GetMapping("videoId/{videoId}")
    fun getTagsByVideoId(@PathVariable videoId: Int): ResponseEntity<String> {
        val video = videoQuery.getVideoById(videoId) ?: return ResponseEntity.notFound().build()
        val tags = tagsQuery.getAllOnVideo(video) ?: return ResponseEntity.noContent().build()
        val frames = frameQuery.getAllFramesOnVideo(video) ?: return ResponseEntity.noContent().build()

        val exposureData = tags.map { TagExposureMapper.toExposure(
            tag = it,
            amountUsed = tagAnalyticsQuery.getAmountUsed(it, listOf(VideoDataDTO(video, frames))).data.toInt(),
            totalPoints = tagAnalyticsQuery.getTotalPoints(it, listOf(VideoDataDTO(video, frames))).data.toDouble()
        ) }

        return ResponseEntity<String>(ObjectMapper().setSerializationInclusion(Include.NON_NULL).writeValueAsString(exposureData), HttpStatus.OK)
    }
}