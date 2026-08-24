package com.FrameHopper.app.core.ports.in.video;

import com.FrameHopper.app.boundry.dto.VideoDTO;

import java.util.List;

public interface VideoQuery {
    List<VideoDTO> getAllVideos();
    List<VideoDTO> getAllWithNotes();
    VideoDTO getVideoById(int id);
    List<VideoDTO> getVideosByIds(List<Integer> ids);
    VideoDTO getVideoByPath(String path);
    List<VideoDTO> getVideoByName(String name);
}
