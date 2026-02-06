package com.FrameHopper.app.boundry.dto;

public record MetadataDto(
        int totalFrames,
        double frameRate,
        double duration,
        int height,
        int width
) {
}
