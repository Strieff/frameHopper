package com.FrameHopper.app.core.ports.in.video;

import com.FrameHopper.app.core.domain.Video;

public interface UpdateVideoPathCommand {
    void updateVideoPath(Video video, String path);
}
