package com.FrameHopper.app.ffmpegService;

public record VideoInfoDto (
        int totalFrames,
        double durationInSeconds,
        double frameRate,
        int width,
        int height
) {}
