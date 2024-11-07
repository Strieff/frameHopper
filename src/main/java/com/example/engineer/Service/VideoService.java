package com.example.engineer.Service;

import com.example.engineer.Model.Frame;
import com.example.engineer.Model.Video;
import com.example.engineer.Repository.FrameRepository;
import com.example.engineer.Repository.TagRepository;
import com.example.engineer.Repository.VideoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Optional;

@Service
public class VideoService {
    @Autowired
    VideoRepository videoRepository;
    @Autowired
    FrameRepository frameRepository;
    @Autowired
    TagRepository tagRepository;

    public Video addVideoData(Video video, Integer totalFrames, Double frameRate, Double duration, Integer height, Integer width){
        video.setTotalFrames(totalFrames);
        video.setFrameRate(frameRate);
        video.setDuration(duration);
        video.setVideoHeight(height);
        video.setVideoWidth(width);
        return videoRepository.save(video);
    }

    public Video createVideoIfNotExists(File video){
        Optional<Video> exist = videoRepository.findByPath(video.getPath());

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

    public Video getByPath(String path){
        return videoRepository.findByPath(path).orElse(null);
    }

    public Video getById(Integer id){
        return videoRepository.findById(id).orElse(null);
    }

    public List<Video> getAll(){
        return videoRepository.findAll();
    }

    public Video saveVideo(Video video){
        return videoRepository.save(video);
    }

    public boolean exists(String pathOfNewPath) {
        return getByPath(pathOfNewPath) != null;
    }

    @Transactional
    public void deleteVideo(Integer id){
        var toDelete = videoRepository.findById(id).orElse(null);
        if(toDelete == null) return;

        var frameIdList = frameRepository.findAllByVideo(toDelete).stream()
                .map(Frame::getId)
                .toList();

        frameRepository.totalFrameDelete(frameIdList);
        frameRepository.totalFrameDelete(toDelete.getId());

        toDelete.setFrames(null);
        videoRepository.delete(toDelete);
    }

    @Transactional
    public void deleteVideo(Video video){
       videoRepository.delete(video);
    }

    @Transactional
    public void deleteVideo(String path){
        var video = getByPath(path);
        if(video == null) return;

        deleteVideo(video.getId());
    }

}
