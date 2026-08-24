package com.FrameHopper.app.boundry.mappers;

import com.FrameHopper.app.boundry.dto.CommentDTO;
import com.FrameHopper.app.core.domain.Comment;

public class BoundaryCommentMapper {
    public static CommentDTO fromDomain(Comment comment) {
        return new CommentDTO(
                comment.getId(),
                comment.getContent(),
                comment.getListingOrder(),
                comment.getVideoId()
        );
    }

    public static Comment toDomain(CommentDTO dto) {
        return new Comment(
                dto.getId(),
                dto.getContent(),
                dto.getListingOrder(),
                dto.getVideoId()
        );
    }
}
