package com.FrameHopper.app.ui.eventing

import com.FrameHopper.app.boundry.dto.TagDTO

object TagEventDispatcher {
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
    fun dispatchUpdate(tags: List<TagDTO>) {
        listeners.forEach { it.onTagUpdated(tags) }
    }

    @JvmStatic
    fun dispatchCreate(tag: TagDTO) {
        listeners.forEach { it.onTagCreated(tag) }
    }

    @JvmStatic
    fun dispatchCreate(tags: List<TagDTO>) {
        listeners.forEach { it.onTagCreated(tags) }
    }

    @JvmStatic
    fun dispatchDelete(tag: TagDTO) {
        listeners.forEach { it.onTagDeleted(tag) }
    }

    @JvmStatic
    fun dispatchDelete(tags: List<TagDTO>) {
        listeners.forEach { it.onTagDeleted(tags) }
    }
}

interface TagUpdatedEventListener {
    fun onTagUpdated(tag: TagDTO)
    fun onTagUpdated(tags: List<TagDTO>)
    fun onTagCreated(tag: TagDTO)
    fun onTagCreated(tag: List<TagDTO>)
    fun onTagDeleted(tag: TagDTO)
    fun onTagDeleted(tags: List<TagDTO>)
}