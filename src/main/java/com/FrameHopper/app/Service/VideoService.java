package com.FrameHopper.app.Service;

import com.FrameHopper.app.Repository.CommentRepositoryOld;
import com.FrameHopper.app.Model.Frame;
import com.FrameHopper.app.Model.Tag;
import com.FrameHopper.app.Model.Video;
import com.FrameHopper.app.Repository.FrameRepositoryOld;
import com.FrameHopper.app.Repository.VideoRepositoryOld;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VideoService {
    private final VideoRepositoryOld videoRepositoryOld = null;
    private final FrameRepositoryOld frameRepositoryOld = null;
    private final CommentRepositoryOld commentRepositoryOld = null;

    public Video createOrGet(File video){
        return videoRepositoryOld.findByPath(video.getPath()).orElseGet(() -> {

            Object data = null;

            return videoRepositoryOld.save(
                    Video.builder()
                            .name(video.getName().replace(" ", "%20"))
                            .path(video.getAbsolutePath())
                            .build()
            );
        });
    }

    public Video getByPath(String path){
        return videoRepositoryOld.findByPath(path).orElse(null);
    }

    public Video getById(Integer id){
        return videoRepositoryOld.findById(id).orElse(null);
    }

    public List<Video> getById(ArrayList<Integer> ids){
        return videoRepositoryOld.findById(ids);
    }

    public List<Video> getAll(){
        return videoRepositoryOld.findAll();
    }

    public List<Video> getAll(List<Integer> ids){
        return videoRepositoryOld.findById(ids);
    }

    public List<Video> getAllData(boolean getNotes){
        var videos = getAll();

        if(videos.isEmpty())
            return Collections.emptyList();

        videos.forEach(v -> v.setFrames(new ArrayList<>()));

        var allFrames = frameRepositoryOld.findAllWithVideos();

        Map<Integer, List<Frame>> framesByVideoId = allFrames.stream()
                .collect(Collectors.groupingBy(f -> f.getVideo().getId()));

        for (Video v : videos) {
            List<Frame> frames = framesByVideoId.getOrDefault(v.getId(), List.of());

            if(frames == null || frames.isEmpty()) continue;
            frames.sort(Comparator.comparingInt(Frame::getFrameNumber)); // low -> high
            v.getFrames().addAll(frames);
        }

        if(getNotes)
            for(Video v : videos)
                v.setComments(commentRepositoryOld.getCommentByVideo(v));

        return videos;
    }

    public List<Video> getVideoData(int id, boolean getNotes){
        return getAllData(getNotes).stream()
                .filter(v -> v.getId() == id)
                .toList();
    }

    public Video saveVideo(Video video){
        return videoRepositoryOld.save(video);
    }

    public boolean exists(String pathOfNewPath) {
        return getByPath(pathOfNewPath) != null;
    }

    public boolean exists(int id) {
        return getById(id) != null;
    }

    @Transactional
    public void deleteVideo(Integer id){
        var toDelete = videoRepositoryOld.findById(id).orElse(null);
        if(toDelete == null) return;

        var frameIdList = frameRepositoryOld.findAllByVideo(toDelete).stream()
                .map(Frame::getId)
                .toList();

        frameRepositoryOld.totalFrameDelete(frameIdList);
        frameRepositoryOld.totalFrameDelete(toDelete.getId());

        toDelete.setFrames(null);
        videoRepositoryOld.delete(toDelete);
    }

    public double getTotalPoints(Video video) {
        return video.getFrames().stream()
                .flatMap(f -> f.getTags().stream())
                .mapToDouble(Tag::getValue)
                .sum();
    }

    public double getComplexity(Video video) {
        return getTotalPoints(video)/video.getDuration();
    }

    public List<Video> getAllWithNotes() {
        return videoRepositoryOld.findAllWithNotes();
    }

    public Video findById(int videoId) {
        return videoRepositoryOld.findById(videoId).orElse(null);
    }
}
