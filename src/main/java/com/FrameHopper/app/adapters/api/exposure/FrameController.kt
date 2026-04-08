package com.FrameHopper.app.adapters.api.exposure

import com.FrameHopper.app.adapters.api.mappers.FrameExposureMapper
import com.FrameHopper.app.core.ports.`in`.frame.FrameQuery
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
@RequestMapping("/api/frames")
class FrameController(
    val frameQuery: FrameQuery,
    val videoQuery: VideoQuery
) {
    @GetMapping("/videoId/{videoId}/frameNo/{frameNo}")
    fun getFrameByVideoIdAndFrameNumber(@PathVariable videoId: Int, @PathVariable frameNo: Int): ResponseEntity<String> {
        val video = videoQuery.getVideoById(videoId) ?: return ResponseEntity.notFound().build()
        if(frameNo - 1 < 0 || frameNo - 1 >= video.metadata.totalFrames) return ResponseEntity.badRequest().build()
        val frame = frameQuery.get(video, frameNo) ?: return ResponseEntity.noContent().build()

        val exposureData = FrameExposureMapper.toExposure(frame)

        return ResponseEntity<String>(ObjectMapper().setSerializationInclusion(Include.NON_NULL).writeValueAsString(exposureData), HttpStatus.OK)
    }

    @GetMapping("/videoId/{videoId}")
    fun getFramesByVideoId(@PathVariable videoId: Int): ResponseEntity<String> {
        val video = videoQuery.getVideoById(videoId) ?: return ResponseEntity.notFound().build()
        val frames = frameQuery.getAllFramesOnVideo(video) ?: return ResponseEntity.noContent().build()

        val exposureData = frames.map(FrameExposureMapper::toExposure)

        return ResponseEntity<String>(ObjectMapper().setSerializationInclusion(Include.NON_NULL).writeValueAsString(exposureData), HttpStatus.OK)
    }
}