package com.FrameHopper.app.adapters.api.exposure

import com.FrameHopper.app.adapters.api.mappers.CommentExposureMapper.fromExposure
import com.FrameHopper.app.adapters.api.mappers.CommentExposureMapper.toExposure
import com.FrameHopper.app.adapters.api.model.`in`.CommentInputDTO
import com.FrameHopper.app.adapters.api.model.out.CommentExposureDTO
import com.FrameHopper.app.boundry.dto.CommentDTO
import com.FrameHopper.app.core.ports.`in`.comment.ChangeCommentContentCommand
import com.FrameHopper.app.core.ports.`in`.comment.ChangeCommentListingOrderCommand
import com.FrameHopper.app.core.ports.`in`.comment.CommentsQuery
import com.FrameHopper.app.core.ports.`in`.comment.CreateCommentCommand
import com.FrameHopper.app.core.ports.`in`.comment.DeleteCommentCommand
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
@RequestMapping("/api/notes")
class CommentController(
    val commentsQuery: CommentsQuery,
    val createCommentCommand: CreateCommentCommand,
    val updateCommentCommand: ChangeCommentContentCommand,
    val changeCommentListingOrderCommand: ChangeCommentListingOrderCommand,
    val deleteCommentCommand: DeleteCommentCommand,
    val videoQuery: VideoQuery
) {
    //region GET
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
        val exposureData = comments.map { it.toExposure() }

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
        val exposureData = comment.toExposure()

        return ResponseEntity.ok(exposureData)
    }
    //endregion

    //region POST
    @Operation(summary = "Create comment")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Comment created",
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
    fun createComment(
        @Parameter(description = "Comment input")
        @RequestBody @Valid commentInput: CommentInputDTO
    ): ResponseEntity<CommentExposureDTO> {
        val video = videoQuery.getVideoById(commentInput.videoId) ?: return ResponseEntity.notFound().build()
        val comments = commentsQuery.getAllCommentsByVideo(video)
        val listingOrder = commentInput.listingOrder ?: if (!comments.isNullOrEmpty()) comments.size - 1 else 0

        val exposureData = commentInput.fromExposure(listingOrder = listingOrder)
            .let {
                createCommentCommand.CreateComment(it)
            }?.toExposure()

        if(!comments.isEmpty()) updateListingOrder(comments, commentInput)

        return ResponseEntity.ok(exposureData)
    }
    //endregion

    //region PATCH
    @Operation(summary = "Update comment")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Comment updated",
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request body",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Comment/Video not found",
            )
        ]
    )
    @PatchMapping("/update")
    fun updateComment(
        @Parameter(description = "Comment input")
        @RequestBody @Valid commentInput: CommentInputDTO
    ): ResponseEntity<CommentExposureDTO> {
        val video = videoQuery.getVideoById(commentInput.videoId) ?: return ResponseEntity.notFound().build()
        commentsQuery.getCommentById(commentInput.id ?: return ResponseEntity.notFound().build())
        val comments = commentsQuery.getAllCommentsByVideo(video) ?: mutableListOf()

        val exposureData = updateCommentCommand.updateCommentContent(
            commentInput.fromExposure(listingOrder = commentInput.listingOrder ?: comments.size)
        ).toExposure()

        if(!comments.isEmpty()) updateListingOrder(comments, commentInput)

        return ResponseEntity.ok(exposureData)
    }
    //endregion

    //region DELETE
    @Operation(summary = "Delete comment")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Comment deleted",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Comment not found",
            )
        ]
    )
    @DeleteMapping("/delete/{id}")
    fun deleteComment(
        @Parameter(description = "ID of the comment", example = "5")
        @PathVariable id: Int
    ): ResponseEntity<Void> {
        val comment = commentsQuery.getCommentById(id) ?: return ResponseEntity.notFound().build()
        val video = videoQuery.getVideoById(comment.videoId)
        val comments = commentsQuery.getAllCommentsByVideo(video) ?: mutableListOf()

        deleteCommentCommand.deleteComment(comment.id)

        if(!comments.isEmpty()) {
            val toUpdate = comments.filter { it.listingOrder >= (comment.listingOrder) }

            if(!toUpdate.isNotEmpty()) {
                toUpdate.forEach { it.listingOrder -= 1 }
                changeCommentListingOrderCommand.changeCommentListingOrder(toUpdate)
            }
        }

        return ResponseEntity.ok().build()
    }
    //endregion

    private fun updateListingOrder(comments: List<CommentDTO>, commentInput: CommentInputDTO) {
        val toUpdate = comments.filter { it.listingOrder >= (commentInput.listingOrder ?: comments.size) }

        if(!toUpdate.isNotEmpty()) {
            toUpdate.forEach { it.listingOrder += 1 }
            changeCommentListingOrderCommand.changeCommentListingOrder(toUpdate)
        }
    }
}