package com.FrameHopper.app.boundry.mappers;

import com.FrameHopper.app.boundry.dto.CommentDTO;
import com.FrameHopper.app.boundry.dto.MetadataDto;
import com.FrameHopper.app.boundry.dto.VideoDTO;
import com.FrameHopper.app.core.domain.Comment;
import com.FrameHopper.app.core.domain.Video;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

        var list = video.getNotes();
        ArrayList<CommentDTO> commentDTOs = (list == null || list.isEmpty()) ? new ArrayList<>() :
                list.stream().map(CommentMapper::fromDomain).collect(Collectors.toCollection(ArrayList::new));


        return new VideoDTO(
                video.getId(),
                video.getName(),
                video.getPath(),
                metadata,
                commentDTOs
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

        var dtoList = videoDTO.comments();
        List<Comment> comments = (dtoList == null || dtoList.isEmpty()) ? new ArrayList<>() :
                dtoList.stream().map(CommentMapper::toDomain).toList();

        return new Video(
                videoDTO.id(),
                videoDTO.path(),
                videoDTO.name(),
                metadata,
                comments
        );
    }
}
