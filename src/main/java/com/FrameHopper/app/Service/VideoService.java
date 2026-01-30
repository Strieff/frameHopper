package com.FrameHopper.app.Service;

import com.FrameHopper.app.Repository.CommentRepository;
import com.FrameHopper.app.ffmpegService.FfmpegService;
import com.FrameHopper.app.ffmpegService.VideoInfoDto;
import com.FrameHopper.app.Model.Frame;
import com.FrameHopper.app.Model.Tag;
import com.FrameHopper.app.Model.Video;
import com.FrameHopper.app.Repository.FrameRepository;
import com.FrameHopper.app.Repository.VideoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VideoService {
    private final VideoRepository videoRepository;
    private final FrameRepository frameRepository;
    private final CommentRepository commentRepository;
    private final FfmpegService ffmpegService;

    public VideoService(
            VideoRepository videoRepository,
            FrameRepository frameRepository,
            CommentRepository commentRepository,
            FfmpegService ffmpegService
    ) {
        this.videoRepository = videoRepository;
        this.frameRepository = frameRepository;
        this.commentRepository = commentRepository;
        this.ffmpegService = ffmpegService;
    }

    public Video createOrGet(File video){
        return videoRepository.findByPath(video.getPath()).orElseGet(() -> {

            VideoInfoDto data;
            try {
                data = ffmpegService.getVideoInfo(video.getAbsolutePath());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            return videoRepository.save(
                    Video.builder()
                            .name(video.getName().replace(" ", "%20"))
                            .path(video.getAbsolutePath())
                            .frameRate(data.frameRate())
                            .duration(data.durationInSeconds())
                            .totalFrames(data.totalFrames())
                            .videoHeight(data.height())
                            .videoWidth(data.width())
                            .build()
            );
        });
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

    public List<Video> getAll(List<Integer> ids){
        return videoRepository.findById(ids);
    }

    public List<Video> getAllData(boolean getNotes){
        var videos = getAll();

        if(videos.isEmpty())
            return Collections.emptyList();

        videos.forEach(v -> v.setFrames(new ArrayList<>()));

        var allFrames = frameRepository.findAllWithVideos();

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
                v.setComments(commentRepository.getCommentByVideo(v));

        return videos;
    }

    public List<Video> getVideoData(int id, boolean getNotes){
        return getAllData(getNotes).stream()
                .filter(v -> v.getId() == id)
                .toList();
    }

    public Video saveVideo(Video video){
        return videoRepository.save(video);
    }

    public boolean exists(String pathOfNewPath) {
        return getByPath(pathOfNewPath) != null;
    }

    public boolean exists(int id) {
        return getById(id) != null;
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
                .flatMap(f -> f.getTags().stream())
                .mapToDouble(Tag::getValue)
                .sum();
    }

    public double getComplexity(Video video) {
        return getTotalPoints(video)/video.getDuration();
    }

    public List<Video> getAllWithNotes() {
        return videoRepository.findAllWithNotes();
    }

    public Video findById(int videoId) {
        return videoRepository.findById(videoId).orElse(null);
    }
}
