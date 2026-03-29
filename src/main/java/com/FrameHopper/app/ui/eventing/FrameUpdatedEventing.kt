package com.FrameHopper.app.ui.eventing

import com.FrameHopper.app.boundry.dto.FrameDTO

object FrameUpdatedEventDispatcher {
    private val listeners = mutableListOf<FrameUpdatedEventListener>()

    @JvmStatic
    fun register(listener: FrameUpdatedEventListener) {
        listeners.add(listener)
    }

    @JvmStatic
    fun unregister(listener: FrameUpdatedEventListener) {
        listeners.remove(listener)
    }

    @JvmStatic
    fun dispatch(frameNumber: Int, frame: FrameDTO?) {
        listeners.forEach { it.onFrameUpdate(frameNumber, frame) }
    }
}

fun interface FrameUpdatedEventListener {
    fun onFrameUpdate(frameNumber: Int, frame: FrameDTO?)
}