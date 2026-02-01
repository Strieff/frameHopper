package com.FrameHopper.app.core.ports.in.video;

import com.FrameHopper.app.core.domain.Video;

public interface DeleteVideoCommand {
    void deleteVideo(int id);
}
