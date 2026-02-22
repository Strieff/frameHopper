package com.FrameHopper.app.ui.eventing

import com.FrameHopper.app.boundry.dto.VideoDTO

object VideoPathUpdatedEventDispatcher {
    private val listeners = mutableListOf<VideoPathUpdatedListener>()

    @JvmStatic
    fun register(listener: VideoPathUpdatedListener) {
        listeners.add(listener)
    }

    @JvmStatic
    fun unregister(listener: VideoPathUpdatedListener) {
        listeners.remove(listener)
    }

    @JvmStatic
    fun dispatch(video: VideoDTO) {
        listeners.forEach { it.onVideoPathUpdated(video) }
    }
}

fun interface VideoPathUpdatedListener {
    fun onVideoPathUpdated(video: VideoDTO)
}