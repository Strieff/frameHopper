package com.FrameHopper.app.core.ports.out.repository;

import com.FrameHopper.app.core.domain.Comment;
import com.FrameHopper.app.core.domain.Video;

import java.util.List;

public interface CommentRepositoryPort {
    Comment getById(int id);
    List<Comment> getAllByVideo(Video video);
    Comment create(Comment comment);
    Comment update(Comment comment);
    void delete(int id);
}
