package com.FrameHopper.app.adapters.ffmpeg

import com.FrameHopper.app.core.domain.Video
import com.FrameHopper.app.core.ports.out.FfmpegPort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException

@Component
open class FfmpegAdapter(
    val ffmpegService: FfmpegService,
    val frameCache: FrameCache
) : FfmpegPort {
    private val logger = LoggerFactory.getLogger(FfmpegAdapter::class.java)

    companion object {
        private const val PRE_LOADING_AMOUNT = 20
    }

    private val inFlight = ConcurrentHashMap.newKeySet<Int>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var prefetchJob: Job? = null

    override fun loadVideo(video: Video) {
        prefetchJob?.cancel()
        inFlight.clear()
        frameCache.clear()

        schedulePrefetch(video, 0)
    }

    override fun getFrameBytes(
        video: Video,
        index: Int
    ): ByteArray {
        frameCache.get(index)?.let { return it }

        schedulePrefetch(video, index)

        return ffmpegService.extractFrameBytes(video.path, index)
    }

    override fun getVideoMetadata(path: String): Video.VideoMetadata = ffmpegService.getVideoInfo(path)

    private fun schedulePrefetch(video: Video, currentIndex: Int) {
        val half = PRE_LOADING_AMOUNT/2
        val from = maxOf(0, currentIndex - half)
        val to = minOf(video.metadata.totalFrames - 1, currentIndex + half)

        prefetchJob?.cancel()
        prefetchJob = scope.launch {
            for (i in (currentIndex + 1)..to) {
                singlePrefetch(video.path, i)
            }

            for (i in (currentIndex - 1) downTo from) {
                singlePrefetch(video.path, i)
            }
        }
    }

    private suspend fun singlePrefetch(path: String, index: Int) {
        if (frameCache.containsKey(index)) return
        if(!inFlight.add(index)) return

        try {
            val bytes = withContext(Dispatchers.IO) {
                ffmpegService.extractFrameBytes(path, index)
            }
            frameCache.put(index, bytes)
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            logger.error("Failed to prefetch video data for index {}", index, e)
        } catch (e: InterruptedException) {
            logger.error("Failed to prefetch video data for index {}", index, e)
        } finally {
            inFlight.remove(index)
        }
    }
}