package com.FrameHopper.app.boundry.dto

import java.util.Objects

data class TagDTO @JvmOverloads constructor(
    val id: Int = -1,
    var name: String,
    var value: Double?,
    var description: String,
    var visible: Boolean = true,
) {
    fun changeStatus() {
        visible = !visible
    }

    override fun equals(other: Any?) = (other is TagDTO) && (id == other.id)

    override fun hashCode() = Objects.hash(id)
}
