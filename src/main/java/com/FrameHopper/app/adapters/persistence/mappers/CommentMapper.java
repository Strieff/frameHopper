package com.FrameHopper.app.adapters.persistence.mappers;

import com.FrameHopper.app.adapters.persistence.entities.CommentEntity;
import com.FrameHopper.app.core.domain.Comment;

public class CommentMapper {
    public static Comment toDomain(CommentEntity commentEntity) {
        return new Comment(
                commentEntity.getId(),
                commentEntity.getContent(),
                commentEntity.getListingOrder()
        );
    }

    public static CommentEntity fromDomain(Comment comment) {
        var commentEntity = new CommentEntity();

        if(comment.getId() != -1)
            commentEntity.setId(comment.getId());

        commentEntity.setContent(comment.getContent());
        commentEntity.setListingOrder(comment.getListingOrder());

        return commentEntity;
    }
}
