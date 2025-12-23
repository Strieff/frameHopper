package com.example.engineer.FfmpegService;

public record VideoInfoDto (
        int totalFrames,
        double durationInSeconds,
        double frameRate,
        int width,
        int height
) {}
