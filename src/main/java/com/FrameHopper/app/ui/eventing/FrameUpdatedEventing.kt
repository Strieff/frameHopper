package com.FrameHopper.app.ui.eventing

import com.FrameHopper.app.boundry.dto.FrameDTO

object FrameUpdatedEventDispatcher {
    private val listeners = mutableListOf<FrameUpdatedListener>()

    @JvmStatic
    fun register(listener: FrameUpdatedListener) {
        listeners.add(listener)
    }

    @JvmStatic
    fun unregister(listener: FrameUpdatedListener) {
        listeners.remove(listener)
    }

    @JvmStatic
    fun dispatch(index: Int, frame: FrameDTO) {
        listeners.forEach { it.onFrameUpdate(index, frame) }
    }
}

fun interface FrameUpdatedListener {
    fun onFrameUpdate(index: Int, frame: FrameDTO)
}