package com.FrameHopper.app.core.application.comment;

import com.FrameHopper.app.core.domain.Comment;
import com.FrameHopper.app.core.domain.Video;
import com.FrameHopper.app.core.ports.in.comment.CommentsQuery;
import com.FrameHopper.app.core.ports.out.repository.CommentRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CommentQueryService implements CommentsQuery {
    private final CommentRepositoryPort commentRepositoryPort;

    @Override
    public Comment getCommentById(int id) {
        return commentRepositoryPort.getById(id);
    }

    @Override
    public List<Comment> getAllCommentsByVideo(Video video) {
        return commentRepositoryPort.getAllByVideo(video);
    }

    @Override
    public int getCommentsCountByVideo(Video video) {
        return commentRepositoryPort.getAllByVideo(video).size();
    }
}
