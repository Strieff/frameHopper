package com.FrameHopper.app.core.application.comment;

import com.FrameHopper.app.core.domain.Comment;
import com.FrameHopper.app.core.ports.in.comment.ChangeCommentContentCommand;
import com.FrameHopper.app.core.ports.in.comment.ChangeCommentListingOrderCommand;
import com.FrameHopper.app.core.ports.in.comment.CreateCommentCommand;
import com.FrameHopper.app.core.ports.in.comment.DeleteCommentCommand;

public class CommentCommandService implements
        ChangeCommentContentCommand,
        ChangeCommentListingOrderCommand,
        CreateCommentCommand,
        DeleteCommentCommand {
    @Override
    public void updateCommentContent(Comment comment, String content) {

    }

    @Override
    public void changeCommentListingOrder(Comment comment, int order) {

    }

    @Override
    public Comment CreateComment(Comment comment) {
        return null;
    }

    @Override
    public void DeleteComment(Comment comment) {

    }
}
