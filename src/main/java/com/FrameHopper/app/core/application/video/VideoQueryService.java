package com.FrameHopper.app.core.application.video;

import com.FrameHopper.app.core.domain.Video;
import com.FrameHopper.app.core.ports.in.video.VideoMetadataQuery;
import com.FrameHopper.app.core.ports.in.video.VideoQuery;

import java.util.List;

public class VideoQueryService implements VideoQuery, VideoMetadataQuery {

    @Override
    public List<Video> getAllVideos() {
        return List.of();
    }

    @Override
    public Video getVideoById(int id) {
        return null;
    }

    @Override
    public Video getVideoByPath(String path) {
        return null;
    }

    @Override
    public Video.VideoMetadata getVideoMetadataById(int id) {
        return null;
    }

    @Override
    public Video.VideoMetadata getVideoMetadataByPath(String path) {
        return null;
    }
}
