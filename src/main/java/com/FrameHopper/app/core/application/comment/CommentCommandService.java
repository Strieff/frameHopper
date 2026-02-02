package com.FrameHopper.app.core.application.comment;

import com.FrameHopper.app.core.domain.Comment;
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
    public void updateCommentContent(Comment comment) {
        commentRepositoryPort.update(comment);
    }

    @Override
    public void changeCommentListingOrder(Comment comment) {
        if(comment.getListingOrder() < 0)
            throw new IllegalArgumentException("Comment listing order must be greater than or equal to 0");

        commentRepositoryPort.update(comment);
    }

    @Override
    public Comment CreateComment(Comment comment) {
        return commentRepositoryPort.create(comment);
    }

    @Override
    public void DeleteComment(int id) {
        commentRepositoryPort.delete(id);
    }
}
