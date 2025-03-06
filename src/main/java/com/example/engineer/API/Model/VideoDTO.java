package com.example.engineer.API.Model;

import com.example.engineer.API.Mapper.FrameMapper;
import com.example.engineer.Model.Video;
import com.google.gson.GsonBuilder;

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

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
}
