package com.FrameHopper.app.adapters.api.model.out

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Output model of a video")
data class VideoExposureDTO(
    @field:Schema(
        description = "ID of the video",
        example = "1",
    )
    val id: Int,

    @field:Schema(
        description = "Name of the video",
        example = "Video34.mp4",
    )
    val name: String,

    @field:Schema(
        description = "Metadata of the video"
    )
    val metadata: VideoMetadataExposureDTO,

    @field:Schema(
        description = "Frames of the video",
    )
    val frames: List<FrameExposureDTO>?,

    @field:Schema(
        description = "Comments on the video",
    )
    val notes: List<CommentExposureDTO>?,

    @field:Schema(
        description = "Analytics value of how many unique tags are used on the video",
        example = "7",
    )
    val uniqueTags: Int?,

    @field:Schema(
        description = "Analytics value of the total sum of all values of tags used on the video",
        example = "3250.5",
    )
    val totalPoints: Double?,

    @field:Schema(
        description = "Analytics value of total calculated complexity of the video",
        example = "3345.23",
    )
    val complexity: Double?
)

@Schema(description = "Output model of metadata of a video")
data class VideoMetadataExposureDTO(
    @field:Schema(
        description = "Total frames of the video",
        example = "350",
    )
    val totalFrames: Int,

    @field:Schema(
        description = "Framerate of the video",
        example = "24.0",
    )
    val frameRate: Double,

    @field:Schema(
        description = "Duration, in seconds, of the video",
        example = "3.58",
    )
    val duration: Double,
)

@Schema(description = "Output model of analytics of a video")
data class VideoAnalyticsExposureDTO(
    @field:Schema(
        description = "Name of the video",
        example = "Video.mp4",
    )
    val name: String,

    @field:Schema(
        description = "Amount of frames on the video",
        example = "244",
    )
    val frameAmount: Int?,

    @field:Schema(
        description = "Framerate of the video",
        example = "30.0",
    )
    val frameRate: Double?,

    @field:Schema(
        description = "Duration in seconds of the video",
        example = "21.37",
    )
    val duration: Double?,

    @field:Schema(
        description = "Amount of unique codes on the video",
        example = "10",
    )
    val uniqueCodes: Int?,

    @field:Schema(
        description = "Total points on the video",
        example = "244.0",
    )
    val totalPoints: Double?,

    @field:Schema(
        description = "Complexity of the video",
        example = "1250.5",
    )
    val complexity: Double?
)

@Schema(description = "Output model of analytics of given set of videos")
data class VideoDataAnalyticsExposureDTO(
    @field:Schema(
        description = "Analytics of videos",
    )
    val analytics: List<VideoAnalyticsExposureDTO>,

    @field:Schema(
        description = "Total shot amount",
        example = "10",
    )
    val totalShotAmount: Int,

    @field:Schema(
        description = "Total frame amount of all videos",
        example = "15000",
    )
    val totalFrameAmount: Int?,

    @field:Schema(
        description = "Average frame amount of all videos",
        example = "24.4",
    )
    val averageFrameAmount: Double?,

    @field:Schema(
        description = "Average framerate of all videos",
        example = "30.0",
    )
    val averageFramerate: Double?,

    @field:Schema(
        description = "Total duration in seconds of all videos",
        example = "240.76",
    )
    val totalDuration: Double?,

    @field:Schema(
        description = "Average duration in seconds of all videos",
        example = "21.37",
    )
    val averageDuration: Double?,

    @field:Schema(
        description = "Average code amount on all videos",
        example = "12.5",
    )
    val averageCodeAmount: Double?,

    @field:Schema(
        description = "Total points of all videos",
        example = "143.2",
    )
    val totalPoints: Double?,

    @field:Schema(
        description = "Average points of all videos",
        example = "21.37",
    )
    val averagePoints: Double?,

    @field:Schema(
        description = "Overall complexity of all videos",
        example = "1430.2",
    )
    val overallComplexity: Double?,

    @field:Schema(
        description = "Average complexity of all videos",
        example = "21.37",
    )
    val averageComplexity: Double?,

    @field:Schema(
        description = "Average shot length",
        example = "4.5",
    )
    val asl: Double?
)
