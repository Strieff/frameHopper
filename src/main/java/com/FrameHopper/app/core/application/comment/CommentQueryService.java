package com.FrameHopper.app.core.application.comment;

import com.FrameHopper.app.core.domain.Comment;
import com.FrameHopper.app.core.domain.Video;
import com.FrameHopper.app.core.ports.in.comment.CommentsQuery;

import java.util.List;

public class CommentQueryService implements CommentsQuery {
    @Override
    public Comment getCommentById(int id) {
        return null;
    }

    @Override
    public List<Comment> getAllCommentsByVideo(Video video) {
        return List.of();
    }

    @Override
    public int getCommentsCountByVideo(Video video) {
        return 0;
    }
}
