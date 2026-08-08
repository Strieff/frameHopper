package com.FrameHopper.app.boundry.dto.export

import com.FrameHopper.app.adapters.TagMap
import com.FrameHopper.app.adapters.VideoMap

enum class ChosenTagAnalytics(
    val label: String,
    val extractor: (TagMap, String) -> Number
)
{
    VALUE(
        label = "export.data.tags.value",
        extractor = TagMap::getValue,
    ),
    AMOUNT(
        label = "export.data.tags.amount",
        extractor = TagMap::getAmount,
    ),
    TOTAL_POINTS(
        label = "export.data.tags.total-points",
        extractor = TagMap::getTotalPoints
    )
}

enum class ChosenVideoAnalytics(
    val label: String,
    val summaryLabel: String?,
    val extractor: (VideoMap, String) -> Number,
    val summaryExtractor: ((VideoMap) -> Number)?,
)
{
    FRAME_COUNT(
        label = "export.data.video.frame-count",
        summaryLabel = "export.data.video.summary.frame-count",
        extractor = VideoMap::getFrameCount,
        summaryExtractor = VideoMap::getTotalFrameCount
    ),
    FRAMERATE(
        label = "export.data.video.framerate",
        summaryLabel = null,
        extractor = VideoMap::getFramerate,
        summaryExtractor = null
    ),
    RUNTIME(
        label = "export.data.video.runtime",
        summaryLabel = "export.data.video.summary.runtime",
        extractor = VideoMap::getRuntime,
        summaryExtractor = VideoMap::getTotalRuntime
    ),
    UNIQUE_TAGS(
        label = "export.data.video.tags",
        summaryLabel = null,
        extractor = VideoMap::getUniqueTags,
        null
    ),
    TOTAL_POINTS(
        label = "export.data.video.total-points",
        summaryLabel = "export.data.video.summary.total-points",
        extractor = VideoMap::getTotalPoints,
        summaryExtractor = VideoMap::getTotalPoints
    ),
    COMPLEXITY(
        label = "export.data.video.complexity",
        summaryLabel = "export.data.video.summary.complexity",
        extractor = VideoMap::getComplexity,
        summaryExtractor = VideoMap::getTotalComplexity
    )
}