package com.FrameHopper.app.core.ports.in.video;

import com.FrameHopper.app.core.domain.Video;

public interface CreateVideoCommand {
    Video createVideo(Video video);
}
