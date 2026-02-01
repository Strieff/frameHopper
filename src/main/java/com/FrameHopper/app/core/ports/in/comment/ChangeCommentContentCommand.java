package com.FrameHopper.app.core.ports.in.comment;

import com.FrameHopper.app.core.domain.Comment;

public interface ChangeCommentContentCommand {
    void updateCommentContent(Comment comment, String content);
}
