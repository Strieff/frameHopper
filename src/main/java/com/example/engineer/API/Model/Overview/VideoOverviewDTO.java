package com.example.engineer.API.Model.Overview;

import com.example.engineer.Model.Video;
import com.google.gson.GsonBuilder;

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

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
}
