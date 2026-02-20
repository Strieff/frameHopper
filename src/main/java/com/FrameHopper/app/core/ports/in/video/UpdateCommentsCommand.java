package com.FrameHopper.app.core.ports.in.video;

import com.FrameHopper.app.boundry.dto.VideoDTO;

public interface UpdateCommentsCommand {
    VideoDTO updateVideo(VideoDTO video);
}
