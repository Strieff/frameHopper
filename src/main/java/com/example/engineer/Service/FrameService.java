package com.example.engineer.Service;

import com.example.engineer.Model.Frame;
import com.example.engineer.Model.Tag;
import com.example.engineer.Model.Video;
import com.example.engineer.Repository.FrameRepository;
import com.example.engineer.Repository.VideoRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FrameService {
    private final FrameRepository frameRepository;
    private final VideoRepository videoRepository;

    public FrameService(FrameRepository frameRepository, VideoRepository videoRepository) {
        this.frameRepository = frameRepository;
        this.videoRepository = videoRepository;
    }

    public void modifyTagsOfFrame(List<Tag> tags, int frameNumber, int id){
        Video video = videoRepository.findById(id).orElse(null);

        if(tags.isEmpty()){
            Optional<Frame> frame = frameRepository.findFrameByFrameNumberAndVideo(frameNumber,video);

            if(frame.isPresent()){
                Frame f = frame.get();
                f.setTags(new ArrayList<>());
                frameRepository.delete(f);
            }

            return;
        }

        Frame frame = frameRepository.findFrameByFrameNumberAndVideo(frameNumber,video).stream().findFirst().orElse(null);

        if(frame == null){
            frame = Frame.builder()
                    .frameNumber(frameNumber)
                    .video(video)
                    .tags(new ArrayList<>())
                    .build();
        }

        frame.setTags(null);
        frameRepository.save(frame);

        if(frame.getTags()!=null) {
            frame.getTags().clear();
            frame.getTags().addAll(tags);
        }else{
            frame.setTags(new ArrayList<>(tags));
        }

        frameRepository.save(frame);
    }

    public List<Frame> getAllByVideo(Video video){
        return frameRepository.findAllByVideo(video);
    }

    public Frame getFrame(Video video, int frameNumber){
        return frameRepository.findFrameOnVideo(video,frameNumber).orElse(null);
    }

    public void getAllVideoData(Video video){
        video.setFrames(getAllByVideo(video));
    }

    public void save(Frame frame){
        frameRepository.save(frame);
    }

    public Frame getById(int id){
        return frameRepository.findById((long) id).orElse(null);
    }
}
