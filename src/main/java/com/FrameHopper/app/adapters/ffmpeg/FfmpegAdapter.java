package com.FrameHopper.app.adapters.ffmpeg;

import com.FrameHopper.app.core.domain.Video;
import com.FrameHopper.app.core.ports.out.FfmpegPort;
import org.springframework.stereotype.Component;

@Component
public class FfmpegAdapter implements FfmpegPort {
    @Override
    public byte[] getFrameBytes(String path, int index) {
        return new byte[0];
    }

    @Override
    public Video.VideoMetadata getVideoMetadata(String path) {
        return null;
    }
}
