package com.FrameHopper.app.ui.actions

import com.FrameHopper.app.boundry.dto.FrameDTO
import com.FrameHopper.app.core.ports.`in`.frame.CreateFrameCommand
import com.FrameHopper.app.core.ports.`in`.frame.DeleteFrameCommand
import com.FrameHopper.app.core.ports.`in`.frame.FrameQuery
import com.FrameHopper.app.core.ports.`in`.frame.UpdateFrameCommand
import com.FrameHopper.app.ui.eventing.FrameUpdatedEventDispatcher
import org.springframework.stereotype.Component

@Component
open class HistoryActions (
    private val createFrameCommand: CreateFrameCommand,
    private val updateFrameCommand: UpdateFrameCommand,
    private val deleteFrameCommand: DeleteFrameCommand,
    private val frameQuery: FrameQuery
){
    val cache = object: LinkedHashMap<Int, StateHistory>() {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, StateHistory>): Boolean = this.size > 5
    }

    fun undo(frameNum: Int, lastState: FrameDTO?){
        val history = getStack(frameNum)
        var previous: FrameDTO?
        try {
            previous = history.previous()
        } catch (_: Exception) {
            return
        }

        history.addPrevious(lastState)

        applySnapshot(previous, lastState, frameNum)
    }

    fun redo(frameNum: Int, lastState: FrameDTO?){
        val history = cache[frameNum] ?: return
        var next: FrameDTO?
        try {
            next = history.next()
        } catch (_: Exception) {
            return
        }

        history.addNext(lastState)

        applySnapshot(next, lastState, frameNum)
    }

    private fun applySnapshot(state: FrameDTO?, lastState: FrameDTO?, frameNum: Int) {
        if(state == null) {
            deleteFrameCommand.deleteFrame(lastState?.id ?: -1)
            FrameUpdatedEventDispatcher.dispatch(frameNum, null)
            return
        }

        val frame = if(frameQuery.get(state.id) == null) createFrameCommand.createFrame(state)
        else updateFrameCommand.updateFrame(state)

        FrameUpdatedEventDispatcher.dispatch(frameNum, frame)
    }

    fun new(frameNum: Int, lastState: FrameDTO?) = getStack(frameNum).new(lastState)

    private fun getStack(frameNum: Int): StateHistory = cache.getOrPut(frameNum) { StateHistory() }

    fun clear() = cache.clear()
}

class StateHistory {
    private val undoStack: BoundedStack<Snapshot> = BoundedStack(6)
    private val redoStack: BoundedStack<Snapshot> = BoundedStack(6)

    fun next(): FrameDTO? {
        val snapshot = redoStack.pop() ?: throw Exception("No snapshot found")
        val frame = snapshot.frame
        return if (frame?.id == -1 || frame == null || frame.tags.isEmpty()) null
        else frame
    }

    fun previous(): FrameDTO? {
        val snapshot = undoStack.pop() ?: throw Exception("No snapshot found")
        val frame = snapshot.frame
        return if (frame?.id == -1 || frame == null || frame.tags.isEmpty()) null
        else frame
    }

    fun new(state: FrameDTO?) {
        redoStack.clear()
        undoStack.push(Snapshot(state))
    }

    fun addPrevious(state: FrameDTO?) = redoStack.push(Snapshot(state))

    fun addNext(state: FrameDTO?) = undoStack.push(Snapshot(state))
}

class BoundedStack<T>(private val maxSize: Int) {
    private val deque = ArrayDeque<T>()

    init {
        require(maxSize > 0) { "maxSize must be greater than 0" }
    }

    fun push(item: T) {
        if(deque.size == maxSize) {
            deque.removeFirst()
        }
        deque.addLast(item)
    }

    fun pop(): T? {
        return if (deque.isEmpty()) null else deque.removeLast()
    }

    fun peek(): T? = deque.lastOrNull()

    fun isEmpty(): Boolean = deque.isEmpty()

    fun size(): Int = deque.size

    fun clear() = deque.clear()

    fun toList(): List<T> = deque.toList()
}

class Snapshot(frame: FrameDTO?) {
    val frame: FrameDTO? = frame?.let {
        FrameDTO(
            it.id,
            it.frameNumber(),
            it.video,
            it.tags.toList()
        )
    }
}

