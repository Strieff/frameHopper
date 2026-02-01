package com.FrameHopper.app.core.ports.out;

import com.FrameHopper.app.core.domain.Video;

public interface FfmpegPort {
    byte[] getFrameBytes(String path, int index);
    Video.VideoMetadata getVideoMetadata(String path);
}
