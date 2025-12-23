package com.example.engineer.View.FXViews.VideoDetails;

import com.example.engineer.Model.Video;
import com.example.engineer.Service.VideoService;
import org.springframework.stereotype.Service;

@Service
public class VideoManagementDetailsService {
    private final VideoService videoService;

    public VideoManagementDetailsService(VideoService videoService) {
        this.videoService = videoService;
    }

    public Video getVideo(int id) {
        return videoService.getById(id);
    }
}
