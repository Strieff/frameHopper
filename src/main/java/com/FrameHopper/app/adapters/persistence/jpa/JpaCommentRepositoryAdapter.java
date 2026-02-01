package com.FrameHopper.app.adapters.persistence.jpa;

import com.FrameHopper.app.Model.Comment;
import com.FrameHopper.app.core.domain.Video;
import com.FrameHopper.app.core.ports.out.repository.CommentRepositoryPort;

import java.util.List;

public class JpaCommentRepositoryAdapter implements CommentRepositoryPort {
    @Override
    public Comment getById(int id) {
        return null;
    }

    @Override
    public List<Comment> getAllByVideo(Video video) {
        return List.of();
    }

    @Override
    public Comment create(Comment comment) {
        return null;
    }

    @Override
    public Comment update(Comment comment) {
        return null;
    }

    @Override
    public void delete(Comment comment) {

    }
}
