package com.FrameHopper.app.core.ports.in.comment;

import com.FrameHopper.app.core.domain.Comment;
import com.FrameHopper.app.core.domain.Video;

import java.util.List;

public interface CommentsQuery {
    Comment getCommentById(int id);
    List<Comment> getAllCommentsByVideo(Video video);
    int getCommentsCountByVideo(Video video);
}
