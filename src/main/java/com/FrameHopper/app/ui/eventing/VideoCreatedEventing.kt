package com.FrameHopper.app.ui.eventing

import com.FrameHopper.app.boundry.dto.VideoDTO

object VideoCreatedEventDispatcher {
    private val listeners = mutableListOf<VideoCreatedListener>()

    @JvmStatic
    fun register(listener: VideoCreatedListener) = listeners.add(listener)

    @JvmStatic
    fun unregister(listener: VideoCreatedListener) = listeners.remove(listener)

    @JvmStatic
    fun dispatch(video: VideoDTO) = listeners.forEach { it.onVideoCreated(video) }
}

fun interface VideoCreatedListener {
    fun onVideoCreated(video: VideoDTO)
}