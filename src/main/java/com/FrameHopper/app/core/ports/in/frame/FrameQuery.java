package com.FrameHopper.app.core.ports.in.frame;

import com.FrameHopper.app.boundry.dto.FrameDTO;
import com.FrameHopper.app.boundry.dto.VideoDTO;

import java.util.List;

public interface FrameQuery {
    List<FrameDTO> getAllFramesOnVideo(VideoDTO video);

    List<FrameDTO> getAll();
}
