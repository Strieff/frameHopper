package com.example.engineer.Service;

import com.example.engineer.Model.Frame;
import com.example.engineer.Model.Tag;
import com.example.engineer.Model.Video;
import com.example.engineer.Repository.FrameRepository;
import com.example.engineer.Repository.TagRepository;
import com.example.engineer.Repository.VideoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VideoService {
    @Autowired
    VideoRepository videoRepository;
    @Autowired
    FrameRepository frameRepository;
    @Autowired
    TagRepository tagRepository;

    public Video createVideoIfNotExists(File video){
        Optional<Video> exist = videoRepository.findByPath(video.getPath());

        if(exist.isEmpty()) {
            Video newVideo = Video.builder()
                    .name(video.getName().replace(" ","%20"))
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

    public List<Video> getById(ArrayList<Integer> ids){
        return videoRepository.findById(ids);
    }

    public List<Video> getAll(){
        return videoRepository.findAll();
    }

    public List<Video> getAllData(){
        var videos = getAll();

        if(videos.isEmpty())
            return Collections.emptyList();

        var frames = frameRepository.findAllWithVideos();

        if(frames.isEmpty())
            return videos;

        getUnifiedVideoData(videos,frames);

        return videos;
    }

    public List<Video> getAllData(String name){
        return getAllData().stream()
                .filter(v -> v.getName().equals(name))
                .toList();
    }

    private void getUnifiedVideoData(List<Video> videos, List<Frame> frames){
        Map<Video,List<Frame>> videoFramesMap = videos.stream()
                .collect(Collectors.toMap(Function.identity(),v -> new ArrayList<>()));

        for(var frame : frames){
            var frameList = videoFramesMap.get(frame.getVideo());
            frameList.add(frame);
        }

        for(var v : videoFramesMap.keySet())
            v.setFrames(videoFramesMap.get(v));

        videoFramesMap.forEach(Video::setFrames);
    }

    public Video saveVideo(Video video){
        return videoRepository.save(video);
    }

    public boolean exists(String pathOfNewPath) {
        return getByPath(pathOfNewPath) != null;
    }

    public boolean existsByName(String name){
        return !videoRepository.findByName(name).isEmpty();
    }

    public List<Video> getAllByName(String name){
        return videoRepository.findByName(name);
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

    public double getTotalPoints(Video video) {
        return video.getFrames().stream()
                .map(Frame::getTags)
                .flatMap(List::stream)
                .mapToDouble(Tag::getValue)
                .sum();
    }

    public double getComplexity(Video video) {
        return getTotalPoints(video)/video.getDuration();
    }
}
