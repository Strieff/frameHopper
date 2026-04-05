package com.FrameHopper.app.adapters.api.model

data class TagExposureDTO(
    val id: Int,
    val name: String,
    val value: Double,
    val description: String?,
    val visible: Boolean,
    val amountUsed: Int? = null,
    val totalPoints: Double? = null,
)