package com.FrameHopper.app.adapters.api.exposure

import com.FrameHopper.app.adapters.api.mappers.CommentExposureMapper
import com.FrameHopper.app.adapters.api.model.out.CommentExposureDTO
import com.FrameHopper.app.core.ports.`in`.comment.CommentsQuery
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
@RequestMapping("/api/notes")
class CommentController(
    val commentsQuery: CommentsQuery,
    val videoQuery: VideoQuery
) {
    @Operation(summary = "Get comments of a video by video id")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Comments found",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Video not found",
            )
        ]
    )
    @GetMapping("videoId/{videoId}")
    fun getNotesByVideoId(
        @Parameter(description = "ID of the video", example = "5")
        @PathVariable videoId: Int
    ): ResponseEntity<List<CommentExposureDTO>> {
        val video = videoQuery.getVideoById(videoId) ?: return ResponseEntity.notFound().build()
        val comments = commentsQuery.getAllCommentsByVideo(video) ?: return ResponseEntity.ok(mutableListOf())
        val exposureData = comments.map { CommentExposureMapper.toExposure(it) }

        return ResponseEntity.ok(exposureData)
    }

    @Operation(summary = "Get comment by id")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Comment found",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Comment not found",
            )
        ]
    )
    @GetMapping("commentId/{commentId}")
    fun getNoteById(
        @Parameter(description = "ID of the comment", example = "5")
        @PathVariable commentId: Int
    ): ResponseEntity<CommentExposureDTO> {
        val comment = commentsQuery.getCommentById(commentId) ?: return ResponseEntity.notFound().build()
        val exposureData = CommentExposureMapper.toExposure(comment)

        return ResponseEntity.ok(exposureData)
    }
}