package com.FrameHopper.app.adapters.api.exposure

import com.FrameHopper.app.adapters.api.mappers.TagExposureMapper.fromExposure
import com.FrameHopper.app.adapters.api.mappers.TagExposureMapper.toExposure
import com.FrameHopper.app.adapters.api.model.AvailableTagAnalytics
import com.FrameHopper.app.adapters.api.model.`in`.TagAnalyticsInputDTO
import com.FrameHopper.app.adapters.api.model.`in`.TagInputDTO
import com.FrameHopper.app.adapters.api.model.out.TagDataAnalyticsExposureDTO
import com.FrameHopper.app.adapters.api.model.out.TagExposureDTO
import com.FrameHopper.app.boundry.dto.TagDTO
import com.FrameHopper.app.boundry.dto.analytics.VideoDataDTO
import com.FrameHopper.app.core.application.analytics.TagAnalyticsQuery
import com.FrameHopper.app.core.ports.`in`.frame.FrameQuery
import com.FrameHopper.app.core.ports.`in`.tag.CreateTagCommand
import com.FrameHopper.app.core.ports.`in`.tag.DeleteTagCommand
import com.FrameHopper.app.core.ports.`in`.tag.TagsQuery
import com.FrameHopper.app.core.ports.`in`.tag.UpdateTagCommand
import com.FrameHopper.app.core.ports.`in`.video.VideoQuery
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/tags")
open class TagController(
    val tagsQuery: TagsQuery,
    val createTagCommand: CreateTagCommand,
    val updateTagCommand: UpdateTagCommand,
    val deleteTagCommand: DeleteTagCommand,
    val videoQuery: VideoQuery,
    val frameQuery: FrameQuery,
    val tagAnalyticsQuery: TagAnalyticsQuery
) {
    //region GET
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
        val exposureData = tags.map {it.toExposure()}

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
        val exposureData = tag.toExposure()

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
        val exposureData = tag.toExposure()

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

        val exposureData = tags.map { it.toExposure(
            amountUsed = tagAnalyticsQuery.getAmountUsed(it, listOf(VideoDataDTO(video, frames))).data.toInt(),
            totalPoints = tagAnalyticsQuery.getTotalPoints(it, listOf(VideoDataDTO(video, frames))).data.toDouble()
        ) }

        return ResponseEntity.ok(exposureData)
    }
    //endregion

    //region POST
    @Operation(summary = "Create tags")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Tags created",
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request body",
            )
        ]
    )
    @PostMapping("/create")
    fun createTags(
        @Parameter(description = "List of tag inputs")
        @RequestBody @Valid tagInput: List<TagInputDTO>
    ): ResponseEntity<List<TagExposureDTO>> {
        val exposureData = tagInput.map {
            TagDTO(
                name = it.name,
                value = it.value,
                description = it.description,
            )
        }.let {
            createTagCommand.CreateTags(it)
        }.map {
            it.toExposure()
        }

        return ResponseEntity.ok(exposureData)
    }

    @Operation(summary = "Get analytics of requested tags")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Analytics calculated for requested tags",
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request body",
            ),
            ApiResponse(
                responseCode = "404",
                description = "All tags/videos not found",
            )
        ]
    )
    @PostMapping("/analytics")
    fun getAnalytics(
        @Parameter(description = "Analytics input")
        @RequestBody @Valid analyticsInput: TagAnalyticsInputDTO
    ): ResponseEntity<TagDataAnalyticsExposureDTO> {
        val videoData = videoQuery.getVideosByIds(analyticsInput.videoIds) ?: return ResponseEntity.notFound().build()
        val tagData = tagsQuery.getTagsByIds(analyticsInput.tagIds) ?: return ResponseEntity.notFound().build()
        val analytics = analyticsInput.analytics ?: AvailableTagAnalytics.entries.toList()

        val grouped = frameQuery.getAllFramesOnVideos(videoData).groupBy { it.video }
        val exposureData = videoData.associateWith { video ->
            grouped[video] ?: emptyList()
        }.map {
            VideoDataDTO(it.key, it.value)
        }.let {
            tagAnalyticsQuery.getAnalytics(tagData, it)
        }.toExposure(analytics)

        return ResponseEntity.ok(exposureData)
    }
    //endregion

    //region PATCH
    @Operation(summary = "Update tag")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Tag updated",
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request body",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Tag not found",
            )
        ]
    )
    @PatchMapping("/update")
    fun updateTag(
        @Parameter(description = "Tag input")
        @RequestBody @Valid tagInput: TagInputDTO
    ): ResponseEntity<TagExposureDTO> {
        tagsQuery.getTagById(tagInput.id ?: return ResponseEntity.notFound().build())
         val tagDto = tagInput.fromExposure()
        val exposureData = updateTagCommand.UpdateTag(tagDto).toExposure()

        return ResponseEntity.ok(exposureData)
    }
    //endregion

    //region DELETE
    @Operation(summary = "Delete tag")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Tag deleted",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Tag not found",
            )
        ]
    )
    @PatchMapping("/delete/{id}")
    fun deleteTag(
        @Parameter(description = "ID of the tag", example = "5")
        @PathVariable id: Int
    ): ResponseEntity<Void> {
        tagsQuery.getTagById(id) ?: return ResponseEntity.notFound().build()
        deleteTagCommand.deleteTag(id)

        return ResponseEntity.ok().build()
    }
    //endregion
}