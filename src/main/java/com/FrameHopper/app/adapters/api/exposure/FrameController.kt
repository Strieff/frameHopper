package com.FrameHopper.app.adapters.api.exposure

import com.FrameHopper.app.adapters.api.mappers.FrameExposureMapper.fromExposure
import com.FrameHopper.app.adapters.api.mappers.FrameExposureMapper.toExposure
import com.FrameHopper.app.adapters.api.model.`in`.FrameInputDTO
import com.FrameHopper.app.adapters.api.model.`in`.FrameOperation
import com.FrameHopper.app.adapters.api.model.out.FrameExposureDTO
import com.FrameHopper.app.core.ports.`in`.frame.CreateFrameCommand
import com.FrameHopper.app.core.ports.`in`.frame.DeleteFrameCommand
import com.FrameHopper.app.core.ports.`in`.frame.FrameQuery
import com.FrameHopper.app.core.ports.`in`.tag.TagsQuery
import com.FrameHopper.app.core.ports.`in`.video.VideoQuery
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/frames")
class FrameController(
    val frameQuery: FrameQuery,
    val createFrameCommand: CreateFrameCommand,
    val deleteFrameCommand: DeleteFrameCommand,
    val videoQuery: VideoQuery,
    val tagsQuery: TagsQuery
) {
    //region GET
    @Operation(summary = "Get frame of a video by video id and frame number")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Frame found",
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid frame number",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Video not found",
            )
        ]
    )
    @GetMapping("/videoId/{videoId}/frameNo/{frameNo}")
    fun getFrameByVideoIdAndFrameNumber(
        @Parameter(description = "ID of the video", example = "5")
        @PathVariable videoId: Int,

        @Parameter(description = "Number of the frame in the sequence", example = "3")
        @PathVariable frameNo: Int
    ): ResponseEntity<FrameExposureDTO> {
        val video = videoQuery.getVideoById(videoId) ?: return ResponseEntity.notFound().build()
        if(frameNo - 1 < 0 || frameNo - 1 >= video.metadata.totalFrames) return ResponseEntity.badRequest().build()
        val frame = frameQuery.get(video, frameNo) ?: return ResponseEntity.ok().build()

        val exposureData = frame.toExposure()

        return ResponseEntity.ok(exposureData)
    }

    @Operation(summary = "Get all frames of a video by video id")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Frames found",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Video not found",
            )
        ]
    )
    @GetMapping("/videoId/{videoId}")
    fun getFramesByVideoId(
        @Parameter(description = "ID of the video", example = "5")
        @PathVariable videoId: Int
    ): ResponseEntity<List<FrameExposureDTO>> {
        val video = videoQuery.getVideoById(videoId) ?: return ResponseEntity.notFound().build()
        val frames = frameQuery.getAllFramesOnVideo(video) ?: return ResponseEntity.ok().build()

        val exposureData = frames.map {it.toExposure()}

        return ResponseEntity.ok(exposureData)
    }
    //endregion

    //region POST
    @Operation(summary = "Create frame")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Frame created",
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request body",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Video not found",
            )
        ]
    )
    @PostMapping("/create")
    fun createFrame(
        @Parameter(description = "frame input")
        @RequestBody @Valid frameInput: FrameInputDTO
    ): ResponseEntity<FrameExposureDTO> {
        val video = videoQuery.getVideoById(frameInput.videoId) ?: return ResponseEntity.notFound().build()
        val exposureData = frameInput.fromExposure(
            video = video,
            tags = emptyList(),
        ).let {
            createFrameCommand.createFrame(it)
        }.toExposure()

        return ResponseEntity.ok(exposureData)
    }
    //endregion

    //region PATCH
    @Operation(summary = "Update frame")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Frame updated",
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request body",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Frame/Video not found",
            )
        ]
    )
    @PatchMapping("/update")
    fun updateFrame(
        @Parameter(description = "frame input")
        @RequestBody @Valid frameInput: FrameInputDTO
    ): ResponseEntity<FrameExposureDTO> {
        frameInput.operation ?: return ResponseEntity.badRequest().build()
        if(frameInput.data.isNullOrEmpty()) return ResponseEntity.badRequest().build()

        val video = videoQuery.getVideoById(frameInput.videoId) ?: return ResponseEntity.notFound().build()
        val tags = tagsQuery.getTagsOnVideoFrame(video, frameInput.frameNo)

        val exposureData = frameInput.fromExposure(
            video = video,
            tags = tags,
        ).apply {
            val tags = tagsQuery.getTagsByIds(frameInput.data)

            when(frameInput.operation) {
                FrameOperation.INSERT -> this.tags.addAll(tags.filterNot { t -> this.tags.any { it.id == t.id } })
                FrameOperation.DELETE -> this.tags.removeAll(tags)
            }
        }.toExposure()

        if(exposureData.tags.isEmpty()) {
            deleteFrameCommand.deleteFrame(exposureData.id)
            return ResponseEntity.ok().build()
        }

        return ResponseEntity.ok(exposureData)
    }
    //endregion

    //region DELETE
    @Operation(summary = "Delete frame")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Frame deleted",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Frame not found",
            )
        ]
    )
    @DeleteMapping("/delete/{id}")
    fun deleteFrame(
        @Parameter(description = "ID of the frame", example = "5")
        @PathVariable id: Int
    ): ResponseEntity<Void> {
        frameQuery.get(id) ?: return ResponseEntity.notFound().build()
        deleteFrameCommand.deleteFrame(id)

        return ResponseEntity.ok().build()
    }
    //endregion
}