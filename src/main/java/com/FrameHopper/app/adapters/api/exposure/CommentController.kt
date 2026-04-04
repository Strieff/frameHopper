package com.FrameHopper.app.adapters.api.exposure

import com.FrameHopper.app.adapters.api.mappers.CommentMapper
import com.FrameHopper.app.core.ports.`in`.comment.CommentsQuery
import com.FrameHopper.app.core.ports.`in`.video.VideoQuery
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
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
    @GetMapping("videoId/{videoId}")
    fun getNotesByVideoId(@PathVariable videoId: Int): ResponseEntity<String> {
        val video = videoQuery.getVideoById(videoId) ?: return ResponseEntity.notFound().build()
        val comments = commentsQuery.getAllCommentsByVideo(video) ?: return ResponseEntity.notFound().build()
        val exposureData = comments.map { CommentMapper.toExposure(it) }

        return ResponseEntity<String>(ObjectMapper().writeValueAsString(exposureData), HttpStatus.OK)
    }

    @GetMapping("commentId/{commentId}")
    fun getNoteById(@PathVariable commentId: Int): ResponseEntity<String> {
        val comment = commentsQuery.getCommentById(commentId) ?: return ResponseEntity.notFound().build()
        val exposureData = CommentMapper.toExposure(comment)

        return ResponseEntity<String>(ObjectMapper().writeValueAsString(exposureData), HttpStatus.OK)
    }
}