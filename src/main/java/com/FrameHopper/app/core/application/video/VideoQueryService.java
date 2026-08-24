package com.FrameHopper.app.core.application.video;

import com.FrameHopper.app.boundry.dto.VideoDTO;
import com.FrameHopper.app.boundry.mappers.BoundaryVideoMapper;
import com.FrameHopper.app.core.domain.Video;
import com.FrameHopper.app.core.ports.in.video.VideoMetadataQuery;
import com.FrameHopper.app.core.ports.in.video.VideoQuery;
import com.FrameHopper.app.core.ports.out.FfmpegPort;
import com.FrameHopper.app.core.ports.out.repository.VideoRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class VideoQueryService implements VideoQuery, VideoMetadataQuery {
    private final FfmpegPort ffmpegPort;
    private final VideoRepositoryPort videoRepositoryPort;

    @Override
    public List<VideoDTO> getAllVideos() {
        var videos = videoRepositoryPort.getAll();

        if(videos == null || videos.isEmpty())
            return new ArrayList<>();

        return videos.stream().map(BoundaryVideoMapper::fromDomain).toList();
    }

    @Override
    public List<VideoDTO> getAllWithNotes() {
        var videos = videoRepositoryPort.getAllWithNotes();

        if(videos == null || videos.isEmpty())
            return new ArrayList<>();

        return videos.stream().map(BoundaryVideoMapper::fromDomain).toList();
    }

    @Override
    public VideoDTO getVideoById(int id) {
        if(id < 0)
            throw new IllegalArgumentException("");

        var video = videoRepositoryPort.getById(id);

        if(video == null)
            throw new IllegalArgumentException("Video with id " + id + " not found");

        return BoundaryVideoMapper.fromDomain(video);
    }

    @Override
    public List<VideoDTO> getVideosByIds(List<Integer> ids) {
        if(ids == null || ids.isEmpty())
            throw new IllegalArgumentException("id list cannot be empty");

        var videos = videoRepositoryPort.getByIds(ids);

        if(videos == null || videos.isEmpty())
            return null;

        return videos.stream().map(BoundaryVideoMapper::fromDomain).toList();
    }

    @Override
    public VideoDTO getVideoByPath(String path) {
        var video = videoRepositoryPort.getByPath(path);

        if(video == null) return null;

        return BoundaryVideoMapper.fromDomain(video);
    }

    @Override
    public List<VideoDTO> getVideoByName(String name) {
        var videos = videoRepositoryPort.getByName(name);

        if(videos == null || videos.isEmpty()) return null;

        return videos.stream().map(BoundaryVideoMapper::fromDomain).toList();
    }

    @Override
    public Video.VideoMetadata getVideoMetadataById(int id) {
        return videoRepositoryPort.getById(id).getMetadata();
    }

    @Override
    public Video.VideoMetadata getVideoMetadataByPath(String path) throws IOException, InterruptedException {
        return ffmpegPort.getVideoMetadata(path);
    }
}
