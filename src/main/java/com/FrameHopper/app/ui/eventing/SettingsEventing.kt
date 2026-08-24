package com.FrameHopper.app.ui.eventing

object ShowHiddenEventDispatcher {
    private val listeners = mutableListOf<ShowHiddenEventListener>()

    @JvmStatic
    fun register(listener: ShowHiddenEventListener) = listeners.add(listener)

    @JvmStatic
    fun unregister(listener: ShowHiddenEventListener) = listeners.remove(listener)

    @JvmStatic
    fun dispatch() = listeners.forEach { it.onShowHiddenUpdated() }
}

fun interface ShowHiddenEventListener {
    fun onShowHiddenUpdated()
}