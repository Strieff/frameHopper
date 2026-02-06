package com.FrameHopper.app.boundry.mappers;

import com.FrameHopper.app.boundry.dto.MetadataDto;
import com.FrameHopper.app.boundry.dto.VideoDTO;
import com.FrameHopper.app.core.domain.Video;

public class VideoMapper {
    public static VideoDTO fromDomain(Video video) {
        MetadataDto metadata = null;

        if(video.getMetadata() != null)
            metadata = new MetadataDto(
                    video.getMetadata().totalFrames(),
                    video.getMetadata().frameRate(),
                    video.getMetadata().duration(),
                    video.getMetadata().height(),
                    video.getMetadata().width()
            );

        return new VideoDTO(
                video.getId(),
                video.getName(),
                video.getPath(),
                metadata
        );
    }

    public static Video toDomain(VideoDTO videoDTO) {
        Video.VideoMetadata metadata = null;

        if(videoDTO.metadata() != null)
            metadata = new Video.VideoMetadata(
                    videoDTO.metadata().totalFrames(),
                    videoDTO.metadata().frameRate(),
                    videoDTO.metadata().duration(),
                    videoDTO.metadata().height(),
                    videoDTO.metadata().width()
            );

        return new Video(
                videoDTO.id(),
                videoDTO.path(),
                videoDTO.name(),
                metadata
        );
    }
}
