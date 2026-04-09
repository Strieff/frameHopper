package com.FrameHopper.app.core.ports.out;

import com.FrameHopper.app.core.domain.Video;

import java.io.IOException;

public interface FfmpegPort {
    void loadVideo(Video video);
    byte[] getFrameBytes(Video video, int index) throws InterruptedException, IOException;
    Video.VideoMetadata getVideoMetadata(String path) throws InterruptedException, IOException;
}
