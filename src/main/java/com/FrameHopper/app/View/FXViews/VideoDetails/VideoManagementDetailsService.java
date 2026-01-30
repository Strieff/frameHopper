package com.FrameHopper.app.View.FXViews.VideoDetails;

import com.FrameHopper.app.Model.Video;
import com.FrameHopper.app.Service.VideoService;
import org.springframework.stereotype.Service;

@Service
public class VideoManagementDetailsService {
    private final VideoService videoService;

    public VideoManagementDetailsService(VideoService videoService) {
        this.videoService = videoService;
    }

    public Video getVideoByPath(String path) {
        return videoService.getByPath(path);
    }

    public void saveVideo(Video video) {
        videoService.saveVideo(video);
    }
}
