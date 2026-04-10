package com.FrameHopper.app.adapters.api.exposure

import com.FrameHopper.app.adapters.api.mappers.FrameExposureMapper
import com.FrameHopper.app.adapters.api.model.out.FrameExposureDTO
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
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/frames")
class FrameController(
    val frameQuery: FrameQuery,
    val videoQuery: VideoQuery
) {
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

        val exposureData = FrameExposureMapper.toExposure(frame)

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

        val exposureData = frames.map(FrameExposureMapper::toExposure)

        return ResponseEntity.ok(exposureData)
    }
}