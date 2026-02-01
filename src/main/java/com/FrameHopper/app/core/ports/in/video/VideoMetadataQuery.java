package com.FrameHopper.app.core.ports.in.video;

import com.FrameHopper.app.core.domain.Video;

public interface VideoMetadataQuery {
    Video.VideoMetadata getVideoMetadataById(int id);
    Video.VideoMetadata getVideoMetadataByPath(String path);
}
