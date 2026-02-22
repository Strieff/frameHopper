package com.FrameHopper.app.ui.eventing

import com.FrameHopper.app.boundry.dto.VideoDTO

object DeleteVideoEventDispatcher {
    private val listeners = mutableListOf<DeleteVideoEventListener>()

    @JvmStatic
    fun register(listener: DeleteVideoEventListener) {
        listeners.add(listener)
    }

    @JvmStatic
    fun unregister(listener: DeleteVideoEventListener) {
        listeners.remove(listener)
    }

    @JvmStatic
    fun dispatch(video: VideoDTO) {
        listeners.forEach { it.onDeleteVideo(video) }
    }
}

fun interface DeleteVideoEventListener {
    fun onDeleteVideo(video: VideoDTO)
}