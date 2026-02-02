package com.FrameHopper.app.adapters.persistence.mappers;

import com.FrameHopper.app.adapters.persistence.entities.VideoEntity;
import com.FrameHopper.app.core.domain.Video;

public class VideoMapper {
    public static Video toDomain(VideoEntity videoEntity) {
        Video.VideoMetadata metadata = null;

        if(videoEntity.getTotalFrames() != null)
            metadata = new Video.VideoMetadata(
                    videoEntity.getTotalFrames(),
                    videoEntity.getFrameRate(),
                    videoEntity.getDuration(),
                    videoEntity.getVideoHeight(),
                    videoEntity.getVideoWidth()
            );

        return new Video(
                videoEntity.getId(),
                videoEntity.getPath(),
                videoEntity.getName(),
                metadata
        );
    }

    public static VideoEntity fromDomain(Video video) {
        var videoEntity = new VideoEntity();

        if(video.getId() != -1)
            videoEntity.setId(video.getId());

        videoEntity.setPath(video.getPath());
        videoEntity.setName(video.getName());

        if(video.getMetadata() != null) {
            videoEntity.setTotalFrames(video.getMetadata().totalFrames());
            videoEntity.setFrameRate(video.getMetadata().frameRate());
            videoEntity.setDuration(video.getMetadata().duration());
            videoEntity.setVideoHeight(video.getMetadata().height());
            videoEntity.setVideoWidth(video.getMetadata().width());
        }

        return videoEntity;
    }
}
