package com.FrameHopper.app.ui.eventing;

import com.FrameHopper.app.boundry.dto.VideoDTO;

public interface VideoPathUpdatedListener {
    void onVideoPathUpdated(VideoDTO video);
}
