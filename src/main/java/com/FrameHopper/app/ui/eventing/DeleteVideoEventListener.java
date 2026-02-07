package com.FrameHopper.app.ui.eventing;

import com.FrameHopper.app.boundry.dto.VideoDTO;

public interface DeleteVideoEventListener {
    void onDeleteVideo(VideoDTO videoDTO);
}
