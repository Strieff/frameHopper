package com.FrameHopper.app.API.Model;

import com.FrameHopper.app.Model.Comment;

public record CommentDTO(String note) {
    public CommentDTO(Comment comment) {
        this(
                comment.getContent()
        );
    }
}
