package com.FrameHopper.app.ui.eventing

import com.FrameHopper.app.boundry.dto.TagDTO

abstract class TagEventDispatcher<T: Any> {
    protected val listeners = mutableListOf<T>()

    fun registerListener(listener: T) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: T) {
        listeners.remove(listener)
    }
}

object TagCreatedEventDispatcher : TagEventDispatcher<TagCreatedEventListener>() {
    @JvmStatic
    fun register(listener: TagCreatedEventListener) = registerListener(listener)

    @JvmStatic
    fun unregister(listener: TagCreatedEventListener) = unregisterListener(listener)

    @JvmStatic
    fun dispatchCreate(tag: TagDTO) {
        listeners.forEach { it.onTagCreated(tag) }
    }

    @JvmStatic
    fun dispatchCreate(tags: List<TagDTO>) {
        listeners.forEach { it.onTagCreated(tags) }
    }
}

interface TagCreatedEventListener {
    fun onTagCreated(tag: TagDTO)
    fun onTagCreated(tags: List<TagDTO>)
}

object TagUpdatedEventDispatcher : TagEventDispatcher<TagUpdatedEventListener>(){
    @JvmStatic
    fun register(listener: TagUpdatedEventListener) = registerListener(listener)

    @JvmStatic
    fun unregister(listener: TagUpdatedEventListener) = unregisterListener(listener)

    @JvmStatic
    fun dispatchUpdate(tag: TagDTO) {
        listeners.forEach { it.onTagUpdated(tag) }
    }

    @JvmStatic
    fun dispatchUpdate(tags: List<TagDTO>) {
        listeners.forEach { it.onTagUpdated(tags) }
    }
}

interface TagUpdatedEventListener {
    fun onTagUpdated(tag: TagDTO)
    fun onTagUpdated(tags: List<TagDTO>)
}

object TagDeletedEventDispatcher : TagEventDispatcher<TagDeletedEventListener>() {
    @JvmStatic
    fun register(listener: TagDeletedEventListener) = registerListener(listener)

    @JvmStatic
    fun unregister(listener: TagDeletedEventListener) = unregisterListener(listener)

    @JvmStatic
    fun dispatchDelete(tag: TagDTO) {
        listeners.forEach { it.onTagDeleted(tag) }
    }

    @JvmStatic
    fun dispatchDelete(tags: List<TagDTO>) {
        listeners.forEach { it.onTagDeleted(tags) }
    }
}

interface TagDeletedEventListener {
    fun onTagDeleted(tag: TagDTO)
    fun onTagDeleted(tags: List<TagDTO>)
}