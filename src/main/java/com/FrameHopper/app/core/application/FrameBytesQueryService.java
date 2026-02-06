package com.FrameHopper.app.core.application;

import com.FrameHopper.app.boundry.dto.VideoDTO;
import com.FrameHopper.app.boundry.mappers.VideoMapper;
import com.FrameHopper.app.core.ports.in.FrameBytesQuery;
import com.FrameHopper.app.core.ports.out.FfmpegPort;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;

@RequiredArgsConstructor
public class FrameBytesQueryService implements FrameBytesQuery {
    private final FfmpegPort ffmpegPort;

    @Override
    public byte[] getVideoFrame(VideoDTO video, int index) throws IOException, InterruptedException {
        var coreVideo = VideoMapper.toDomain(video);

        if (!new File(video.path()).exists())
            throw new IllegalArgumentException("Video not found!");

        if (index < 0)
            throw new IllegalArgumentException("Invalid index!");

        return ffmpegPort.getFrameBytes(coreVideo, index);
    }
}
