package com.FrameHopper.app.ui.actions

import com.FrameHopper.app.boundry.dto.FrameDTO
import com.FrameHopper.app.boundry.dto.TagDTO
import com.FrameHopper.app.core.ports.`in`.frame.CreateFrameCommand
import com.FrameHopper.app.core.ports.`in`.frame.DeleteFrameCommand
import com.FrameHopper.app.core.ports.`in`.frame.UpdateFrameCommand
import com.FrameHopper.app.ui.eventing.FrameUpdatedEventDispatcher
import org.springframework.stereotype.Component

@Component
open class PasteRecentAction(
    private val createFrameCommand: CreateFrameCommand,
    private val updateFrameCommand: UpdateFrameCommand
) {
    private var recentlyAdded = mutableListOf<TagDTO>()

    fun addTags(tags: List<TagDTO>) {
        if(recentlyAdded.isNotEmpty() && tags.isEmpty()) return

        recentlyAdded.apply { clear(); addAll(tags) }
    }

    fun add(frame: FrameDTO) {
        if(recentlyAdded.isEmpty()) return

        val tagsToAdd = recentlyAdded.filter { frame.tags?.contains(it) == false }
        frame.tags.addAll(tagsToAdd)

        val updatedFrame = if (frame.id == -1) createFrameCommand.createFrame(frame)
        else updateFrameCommand.updateFrame(frame)

        FrameUpdatedEventDispatcher.dispatch(updatedFrame.frameNumber, updatedFrame)
    }
}

@Component
open class RemoveRecentAction(
    private val updateFrameCommand: UpdateFrameCommand,
    private val deleteFrameCommand: DeleteFrameCommand
) {
    private var recentlyRemoved = mutableListOf<TagDTO>()

    fun addTags(tags: List<TagDTO>) {
        if(recentlyRemoved.isNotEmpty() && tags.isEmpty()) return

        recentlyRemoved.apply { clear(); addAll(tags) }
    }

    fun remove(frame: FrameDTO) {
        if(recentlyRemoved.isEmpty()) return

        val tagsToRemove = recentlyRemoved.filter { frame.tags?.contains(it) == true }
        frame.tags.removeAll(tagsToRemove)

        if(frame.tags.isEmpty()) {
            deleteFrameCommand.deleteFrame(frame.id)
            FrameUpdatedEventDispatcher.dispatch(frame.frameNumber, null)
        } else {
            val updatedFrame = updateFrameCommand.updateFrame(frame)
            FrameUpdatedEventDispatcher.dispatch(updatedFrame.frameNumber, updatedFrame)
        }
    }
}