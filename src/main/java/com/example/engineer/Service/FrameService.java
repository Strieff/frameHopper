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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FrameService {
    @Autowired
    FrameRepository frameRepository;
    @Autowired
    TagRepository tagRepository;
    @Autowired
    VideoRepository videoRepository;

    public void modifyTagsOfFrame(List<Tag> tags, int frameNumber, String videoPath){
        Video video = videoRepository.findByPath(videoPath).orElse(null);

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
