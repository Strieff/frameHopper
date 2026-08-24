package com.FrameHopper.app.core.ports.in.video;

import com.FrameHopper.app.core.domain.Video;

import java.io.IOException;

public interface VideoMetadataQuery {
    Video.VideoMetadata getVideoMetadataById(int id);
    Video.VideoMetadata getVideoMetadataByPath(String path) throws IOException, InterruptedException;
}
