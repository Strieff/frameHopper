package com.FrameHopper.app.boundry.dto.export

import com.FrameHopper.app.boundry.dto.VideoDTO

data class ExportDataInput(
    val videos: List<VideoDTO>,
    val chosenVideoAnalytics: List<ChosenVideoAnalytics>,
    val chosenTagAnalytics: List<ChosenTagAnalytics>
)