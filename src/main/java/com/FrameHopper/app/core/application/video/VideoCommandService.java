package com.FrameHopper.app.core.application.video;

import com.FrameHopper.app.adapters.persistence.jpa.JpaVideoRepositoryAdapter;
import com.FrameHopper.app.core.domain.Video;
import com.FrameHopper.app.core.ports.in.video.CreateVideoCommand;
import com.FrameHopper.app.core.ports.in.video.DeleteVideoCommand;
import com.FrameHopper.app.core.ports.in.video.UpdateVideoPathCommand;
import com.FrameHopper.app.core.ports.out.repository.VideoRepositoryPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VideoCommandService implements
        DeleteVideoCommand,
        UpdateVideoPathCommand,
        CreateVideoCommand {
    private final VideoRepositoryPort videoRepositoryPort;

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
}
