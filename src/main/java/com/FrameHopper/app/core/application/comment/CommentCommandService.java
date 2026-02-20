package com.FrameHopper.app.core.application.comment;

import com.FrameHopper.app.boundry.dto.CommentDTO;
import com.FrameHopper.app.boundry.mappers.CommentMapper;
import com.FrameHopper.app.core.ports.in.comment.ChangeCommentContentCommand;
import com.FrameHopper.app.core.ports.in.comment.ChangeCommentListingOrderCommand;
import com.FrameHopper.app.core.ports.in.comment.CreateCommentCommand;
import com.FrameHopper.app.core.ports.in.comment.DeleteCommentCommand;
import com.FrameHopper.app.core.ports.out.repository.CommentRepositoryPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommentCommandService implements
        ChangeCommentContentCommand,
        ChangeCommentListingOrderCommand,
        CreateCommentCommand,
        DeleteCommentCommand {
    private final CommentRepositoryPort commentRepositoryPort;

    @Override
    public CommentDTO updateCommentContent(CommentDTO comment) {
        var coreComment = CommentMapper.toDomain(comment);
        coreComment = commentRepositoryPort.update(coreComment);

        return CommentMapper.fromDomain(coreComment);
    }

    @Override
    public CommentDTO changeCommentListingOrder(CommentDTO comment) {
        if(comment.getListingOrder() < 0)
            throw new IllegalArgumentException("Comment listing order must be greater than or equal to 0");

        var coreComment = CommentMapper.toDomain(comment);
        coreComment = commentRepositoryPort.update(coreComment);

        return CommentMapper.fromDomain(coreComment);
    }

    @Override
    public CommentDTO CreateComment(CommentDTO comment) {
        var coreComment = CommentMapper.toDomain(comment);
        var createdComment = commentRepositoryPort.create(coreComment);

        return CommentMapper.fromDomain(createdComment);
    }

    @Override
    public void DeleteComment(int id) {
        commentRepositoryPort.delete(id);
    }
}
