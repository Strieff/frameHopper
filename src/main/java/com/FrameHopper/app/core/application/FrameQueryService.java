package com.FrameHopper.app.core.application;

import com.FrameHopper.app.core.domain.Video;
import com.FrameHopper.app.core.ports.in.FrameQuery;
import com.FrameHopper.app.core.ports.out.FfmpegPort;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;

@RequiredArgsConstructor
public class FrameQueryService implements FrameQuery {
    private final FfmpegPort ffmpegPort;

    @Override
    public byte[] getVideoFrame(Video video, int index) throws IOException, InterruptedException {
        if (!new File(video.getPath()).exists())
            throw new IllegalArgumentException("Video not found!");

        if (index < 0)
            throw new IllegalArgumentException("Invalid index!");

        return ffmpegPort.getFrameBytes(video, index);
    }
}
