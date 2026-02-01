package com.FrameHopper.app.core.application;

import com.FrameHopper.app.core.ports.in.FrameQuery;
import com.FrameHopper.app.core.ports.out.FfmpegPort;
import lombok.RequiredArgsConstructor;

import java.io.File;

@RequiredArgsConstructor
public class FrameQueryService implements FrameQuery {
    private final FfmpegPort ffmpegPort;

    @Override
    public byte[] getVideoFrame(String path, int index) {
        if (!new File(path).exists())
            throw new IllegalArgumentException("Video not found!");

        if (index < 0)
            throw new IllegalArgumentException("Invalid index!");

        return ffmpegPort.getFrameBytes(path, index);
    }
}
