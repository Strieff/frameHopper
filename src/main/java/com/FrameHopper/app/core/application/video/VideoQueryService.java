package com.FrameHopper.app.core.application.video;

import com.FrameHopper.app.core.domain.Video;
import com.FrameHopper.app.core.ports.in.video.VideoMetadataQuery;
import com.FrameHopper.app.core.ports.in.video.VideoQuery;
import com.FrameHopper.app.core.ports.out.FfmpegPort;
import com.FrameHopper.app.core.ports.out.repository.VideoRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class VideoQueryService implements VideoQuery, VideoMetadataQuery {
    private final FfmpegPort ffmpegPort;
    private final VideoRepositoryPort videoRepositoryPort;

    @Override
    public List<Video> getAllVideos() {
        return videoRepositoryPort.getAll();
    }

    @Override
    public Video getVideoById(int id) {
        return videoRepositoryPort.getById(id);
    }

    @Override
    public Video getVideoByPath(String path) {
        return videoRepositoryPort.getByPath(path);
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
