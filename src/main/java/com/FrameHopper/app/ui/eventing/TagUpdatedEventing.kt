package com.FrameHopper.app.ui.eventing

import com.FrameHopper.app.boundry.dto.TagDTO

object TagUpdatedEventDispatcher {
    private val listeners = mutableListOf<TagUpdatedEventListener>()

    @JvmStatic
    fun register(listener: TagUpdatedEventListener) {
        listeners.add(listener)
    }

    @JvmStatic
    fun unregister(listener: TagUpdatedEventListener) {
        listeners.remove(listener)
    }

    @JvmStatic
    fun dispatchUpdate(tag: TagDTO) {
        listeners.forEach { it.onTagUpdated(tag) }
    }

    @JvmStatic
    fun dispatchCreate(tag: TagDTO) {
        listeners.forEach { it.onTagCreated(tag) }
    }

    @JvmStatic
    fun dispatchDelete(tag: TagDTO) {
        listeners.forEach { it.onTagDeleted(tag) }
    }
}

interface TagUpdatedEventListener {
    fun onTagUpdated(tag: TagDTO)
    fun onTagCreated(tag: TagDTO)
    fun onTagDeleted(tag: TagDTO)
}