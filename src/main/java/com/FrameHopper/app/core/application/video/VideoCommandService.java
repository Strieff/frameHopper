package com.FrameHopper.app.core.application.video;

import com.FrameHopper.app.core.domain.Video;
import com.FrameHopper.app.core.ports.in.video.CreateVideoCommand;
import com.FrameHopper.app.core.ports.in.video.DeleteVideoCommand;
import com.FrameHopper.app.core.ports.in.video.LoadVideoCommand;
import com.FrameHopper.app.core.ports.in.video.UpdateVideoPathCommand;
import com.FrameHopper.app.core.ports.out.FfmpegPort;
import com.FrameHopper.app.core.ports.out.repository.VideoRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

@RequiredArgsConstructor
public class VideoCommandService implements
        DeleteVideoCommand,
        UpdateVideoPathCommand,
        CreateVideoCommand,
        LoadVideoCommand {
    private final VideoRepositoryPort videoRepositoryPort;
    private final FfmpegPort ffmpegPort;

    @Override
    public void deleteVideo(int id) {
        videoRepositoryPort.delete(id);
    }

    @Override
    public Video updateVideoPath(Video video) {
        return videoRepositoryPort.update(video);
    }

    @Override
    public Video createVideo(Video video) {
        return videoRepositoryPort.create(video);
    }

    @Override
    public Video loadVideo(String path) throws IOException, InterruptedException {
        if (!new File(path).exists())
            throw new FileNotFoundException(path);

        var loadedVideo = videoRepositoryPort.getByPath(path);

        if(loadedVideo == null) {
            var metadata = ffmpegPort.getVideoMetadata(path);
            loadedVideo = new Video(
                    -1,
                    path,
                    new File(path).getName(),
                    metadata
            );
            loadedVideo = createVideo(loadedVideo);
        }

        ffmpegPort.loadVideo(loadedVideo);
        return loadedVideo;
    }

    @Override
    public Video loadVideo(int id) throws IOException, InterruptedException {
        var loadedVideo = videoRepositoryPort.getById(id);

        ffmpegPort.loadVideo(loadedVideo);
        return loadedVideo;
    }
}
