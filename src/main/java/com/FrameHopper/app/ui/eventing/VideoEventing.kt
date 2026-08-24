package com.FrameHopper.app.ui.eventing

import com.FrameHopper.app.boundry.dto.VideoDTO

object VideoDeletedEventDispatcher {
    private val listeners = mutableListOf<VideoDeletedEventListener>()

    @JvmStatic
    fun register(listener: VideoDeletedEventListener) = listeners.add(listener)

    @JvmStatic
    fun unregister(listener: VideoDeletedEventListener) = listeners.remove(listener)

    @JvmStatic
    fun dispatch(video: VideoDTO) = listeners.forEach { it.onDeleteVideo(video) }
}

fun interface VideoDeletedEventListener {
    fun onDeleteVideo(video: VideoDTO)
}

object OpenVideoEventDispatcher {
    private val listeners = mutableListOf<OpenVideoEventListener>()

    @JvmStatic
    fun register(listener: OpenVideoEventListener) = listeners.add(listener)

    @JvmStatic
    fun unregister(listener: OpenVideoEventListener) = listeners.remove(listener)

    @JvmStatic
    fun dispatch(id: Int) = listeners.forEach { it.openVideo(id) }
}

fun interface OpenVideoEventListener {
    fun openVideo(id: Int)
}

object VideoPathUpdatedEventDispatcher {
    private val listeners = mutableListOf<VideoPathUpdatedListener>()

    @JvmStatic
    fun register(listener: VideoPathUpdatedListener) = listeners.add(listener)

    @JvmStatic
    fun unregister(listener: VideoPathUpdatedListener) = listeners.remove(listener)

    @JvmStatic
    fun dispatch(video: VideoDTO) = listeners.forEach { it.onVideoPathUpdated(video) }
}

fun interface VideoPathUpdatedListener {
    fun onVideoPathUpdated(video: VideoDTO)
}