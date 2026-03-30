package com.FrameHopper.app.ui.eventing

object NewLanguageEventDispatcher {
    private val listeners = mutableListOf<NewLanguageListener>()

    @JvmStatic
    fun register(listener: NewLanguageListener) = listeners.add(listener)

    @JvmStatic
    fun unregister(listener: NewLanguageListener) = listeners.remove(listener)

    @JvmStatic
    fun dispatch() = listeners.forEach { it.newLanguageCreated() }
}

fun interface NewLanguageListener {
    fun newLanguageCreated()
}