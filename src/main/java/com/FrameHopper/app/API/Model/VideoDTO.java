package com.FrameHopper.app.API.Model;

import com.FrameHopper.app.API.Mapper.FrameMapper;
import com.FrameHopper.app.Model.Video;

import java.util.List;

public record VideoDTO(
        int id,
        String name,
        int totalFrames,
        double frameRate,
        double duration,
        int videoHeight,
        int videoWidth,
        List<FrameDTO> frames
) {
    public VideoDTO(Video video) {
        this(
                video.getId(),
                video.getName(),
                video.getTotalFrames(),
                video.getFrameRate(),
                video.getDuration(),
                video.getVideoHeight(),
                video.getVideoWidth(),
                FrameMapper.mapFrames(video.getFrames())
        );
    }
}
