package com.FrameHopper.app.core.ports.in.comment;

import com.FrameHopper.app.boundry.dto.CommentDTO;
import com.FrameHopper.app.boundry.dto.VideoDTO;
import com.FrameHopper.app.core.domain.Comment;
import com.FrameHopper.app.core.domain.Video;

import java.util.List;

public interface CommentsQuery {
    CommentDTO getCommentById(int id);
    List<CommentDTO> getAllCommentsByVideo(VideoDTO video);
    Integer getCommentsCountByVideo(VideoDTO video);
}
