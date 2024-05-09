package com.example.engineer.Service;

import com.example.engineer.Model.Video;
import com.example.engineer.Repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public Video createVideoIfNotExists(String name){
        Optional<Video> exist = videoRepository.findByName(name);

        if(exist.isEmpty()) {
            Video video = Video.builder()
                    .name(name)
                    .build();
            return videoRepository.save(video);
        }else
            return exist.get();
    }

    public Video getByName(String name){
        return videoRepository.findByName(name).orElse(null);
    }
    public List<Video> getAll(){
        return videoRepository.findAll();
    }

    public Video getExportData(int index){
        return videoRepository.findFirstById(index).orElse(null);
    }

    public List<Video> getExportData(List<Integer> indexList){
        return videoRepository.findAllByIdIn(indexList);
    }
}
