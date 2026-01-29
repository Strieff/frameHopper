package com.FrameHopper.app.API.Model.Overview;

import com.FrameHopper.app.Model.Video;

public record VideoOverviewDTO(
        String name,
        int totalFrames,
        double duration,
        double framerate,
        double totalPoints,
        double complexity
) {
    public VideoOverviewDTO(Video video, double totalPoints, double complexity){
        this(
                video.getName(),
                video.getTotalFrames(),
                video.getDuration(),
                video.getFrameRate(),
                totalPoints,
                complexity
        );
    }
}
