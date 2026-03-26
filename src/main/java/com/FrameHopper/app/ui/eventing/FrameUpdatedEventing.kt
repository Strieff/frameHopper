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
    fun dispatch(frameNumber: Int, frame: FrameDTO?) {
        listeners.forEach { it.onFrameUpdate(frameNumber, frame) }
    }
}

fun interface FrameUpdatedListener {
    fun onFrameUpdate(frameNumber: Int, frame: FrameDTO?)
}