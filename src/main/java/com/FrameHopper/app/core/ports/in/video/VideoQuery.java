package com.FrameHopper.app.core.ports.in.video;

import com.FrameHopper.app.core.domain.Video;

import java.util.List;

public interface VideoQuery {
    List<Video> getAllVideos();
    Video getVideoById(int id);
    Video getVideoByPath(String path);
}
