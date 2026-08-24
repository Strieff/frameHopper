package com.FrameHopper.app.core.ports.in.video;

import com.FrameHopper.app.boundry.dto.VideoDTO;

public interface UpdateVideoPathCommand {
    VideoDTO updateVideoPath(VideoDTO video, String newPath);
}
