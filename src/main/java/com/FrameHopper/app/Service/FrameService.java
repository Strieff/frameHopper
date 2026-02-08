package com.FrameHopper.app.Service;

import com.FrameHopper.app.Model.Frame;
import com.FrameHopper.app.Model.Tag;
import com.FrameHopper.app.Model.Video;
import com.FrameHopper.app.Repository.CommentRepositoryOld;
import com.FrameHopper.app.Repository.FrameRepositoryOld;
import com.FrameHopper.app.Repository.VideoRepositoryOld;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FrameService {
    private final FrameRepositoryOld frameRepositoryOld = null;
    private final VideoRepositoryOld videoRepositoryOld = null;
    private final CommentRepositoryOld commentRepositoryOld = null;

    public void modifyTagsOfFrame(List<Tag> tags, int frameNumber, int id){
        Video video = videoRepositoryOld.findById(id).orElse(null);

        if(tags.isEmpty()){
            Optional<Frame> frame = frameRepositoryOld.findFrameByFrameNumberAndVideo(frameNumber,video);

            if(frame.isPresent()){
                Frame f = frame.get();
                f.setTags(new ArrayList<>());
                frameRepositoryOld.delete(f);
            }

            return;
        }

        Frame frame = frameRepositoryOld.findFrameByFrameNumberAndVideo(frameNumber,video).stream().findFirst().orElse(null);

        if(frame == null){
            frame = Frame.builder()
                    .frameNumber(frameNumber)
                    .video(video)
                    .tags(new ArrayList<>())
                    .build();
        }

        frame.setTags(null);
        frameRepositoryOld.save(frame);

        if(frame.getTags()!=null) {
            frame.getTags().clear();
            frame.getTags().addAll(tags);
        }else{
            frame.setTags(new ArrayList<>(tags));
        }

        frameRepositoryOld.save(frame);
    }

    public List<Frame> getAllByVideo(Video video){
        return frameRepositoryOld.findAllByVideo(video);
    }

    public Frame getFrame(Video video, int frameNumber){
        return frameRepositoryOld.findFrameOnVideo(video,frameNumber).orElse(null);
    }

    public void getAllVideoData(Video video, boolean getNotes){
        video.setFrames(getAllByVideo(video));
        if(getNotes) video.setComments(commentRepositoryOld.getCommentByVideo(video));
    }

    public void save(Frame frame){
        frameRepositoryOld.save(frame);
    }

    public Frame getById(int id){
        return frameRepositoryOld.findById((long) id).orElse(null);
    }
}
