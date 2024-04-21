package com.example.engineer.Service;

import com.example.engineer.Model.Video;
import com.example.engineer.Repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VideoService {
    @Autowired
    VideoRepository videoRepository;

    public void createVideoIfNotExists(String name){
        if(videoRepository.findByName(name).isEmpty()) {
            Video video = new Video();
            video.setName(name);
            videoRepository.save(video);
        }
    }

    public Video getByName(String name){
        return videoRepository.findByName(name).orElse(null);
    }
}
