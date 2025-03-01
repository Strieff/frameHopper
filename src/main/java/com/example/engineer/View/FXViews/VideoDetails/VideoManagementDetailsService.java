package com.example.engineer.View.FXViews.VideoDetails;

import com.example.engineer.Model.Video;
import com.example.engineer.Service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VideoManagementDetailsService {
    @Autowired
    private VideoService videoService;

    public Video getVideo(int id) {
        return videoService.getById(id);
    }
}
