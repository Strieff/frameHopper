package com.FrameHopper.app.core.application.video;

import com.FrameHopper.app.boundry.dto.VideoDTO;
import com.FrameHopper.app.boundry.mappers.VideoMapper;
import com.FrameHopper.app.core.domain.Video;
import com.FrameHopper.app.core.ports.in.video.*;
import com.FrameHopper.app.core.ports.out.FfmpegPort;
import com.FrameHopper.app.core.ports.out.repository.VideoRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

@RequiredArgsConstructor
public class VideoCommandService implements
        DeleteVideoCommand,
        UpdateVideoPathCommand,
        CreateVideoCommand,
        LoadVideoCommand,
        UpdateCommentsCommand {
    private final VideoRepositoryPort videoRepositoryPort;
    private final FfmpegPort ffmpegPort;

    @Override
    public void deleteVideo(int id) {
        videoRepositoryPort.delete(id);
    }

    @Override
    public VideoDTO updateVideoPath(VideoDTO video, String newPath) {
        var existingVideo = videoRepositoryPort.getByPath(newPath);

        if(existingVideo != null)
            throw new IllegalArgumentException("Video already exists");

        var toChange = VideoMapper.toDomain(video);
        toChange.changePath(newPath);
        toChange = videoRepositoryPort.update(toChange);

        return VideoMapper.fromDomain(toChange);
    }

    @Override
    public Video createVideo(Video video) {
        return videoRepositoryPort.create(video);
    }

    @Override
    public VideoDTO loadVideo(String path) throws IOException, InterruptedException {
        if (!new File(path).exists())
            throw new FileNotFoundException(path);

        var loadedVideo = videoRepositoryPort.getByPath(path);

        if(loadedVideo == null) {
            var metadata = ffmpegPort.getVideoMetadata(path);
            loadedVideo = new Video(
                    -1,
                    path,
                    new File(path).getName(),
                    metadata,
                    new ArrayList<>()
            );
            loadedVideo = videoRepositoryPort.create(loadedVideo);
        }

        ffmpegPort.loadVideo(loadedVideo);
        return VideoMapper.fromDomain(loadedVideo);
    }

    @Override
    public VideoDTO loadVideo(int id) {
        var loadedVideo = videoRepositoryPort.getById(id);

        if(loadedVideo == null)
            throw new IllegalArgumentException("Video with id " + id + " not found");

        ffmpegPort.loadVideo(loadedVideo);
        return VideoMapper.fromDomain(loadedVideo);
    }

    @Override
    public VideoDTO updateVideo(VideoDTO video) {
        var coreVideo = VideoMapper.toDomain(video);

        return VideoMapper.fromDomain(videoRepositoryPort.update(coreVideo));
    }
}
