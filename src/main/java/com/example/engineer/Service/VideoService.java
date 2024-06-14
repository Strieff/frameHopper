package com.example.engineer.Service;

import com.example.engineer.Model.Video;
import com.example.engineer.Repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Optional;

@Service
public class VideoService {
    @Autowired
    VideoRepository videoRepository;

    public void addVideoData(Video video, Integer totalFrames, Double frameRate, Double duration, Integer height, Integer width){
        video.setTotalFrames(totalFrames);
        video.setFrameRate(frameRate);
        video.setDuration(duration);
        video.setVideoHeight(height);
        video.setVideoWidth(width);
        videoRepository.save(video);
    }

    public Video createVideoIfNotExists(File video){
        Optional<Video> exist = videoRepository.findByName(video.getName());

        if(exist.isEmpty()) {
            Video newVideo = Video.builder()
                    .name(video.getName())
                    .path(video.getAbsolutePath())
                    .build();
            return videoRepository.save(newVideo);
        }else {
            Video vid = exist.get();

            if(vid.getPath()==null){
                vid.setPath(video.getAbsolutePath());
                videoRepository.save(vid);
            }

            return vid;
        }
    }

    public Video getByName(String name){
        return videoRepository.findByName(name).orElse(null);
    }

    public List<Video> getAll(){
        return videoRepository.findAll();
    }
}
