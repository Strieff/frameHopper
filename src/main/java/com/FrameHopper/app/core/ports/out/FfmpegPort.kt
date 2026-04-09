package com.FrameHopper.app.core.ports.out

import com.FrameHopper.app.core.domain.Video

interface FfmpegPort {
    fun loadVideo(video: Video)
    fun getFrameBytes(video: Video, index: Int): ByteArray
    fun getVideoMetadata(path: String): Video.VideoMetadata
}