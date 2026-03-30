package com.FrameHopper.app.ui.eventing

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