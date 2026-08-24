package com.FrameHopper.app.core.ports.in.frame;

import com.FrameHopper.app.boundry.dto.FrameDTO;
import com.FrameHopper.app.boundry.dto.VideoDTO;

import java.util.List;

public interface FrameQuery {
    FrameDTO get(int id);
    FrameDTO get(VideoDTO video, int frameNo);
    List<FrameDTO> getAllFramesOnVideo(VideoDTO video);
    List<FrameDTO> getAllFramesOnVideos(List<VideoDTO> video);
    List<FrameDTO> getAll();
}
