package com.FrameHopper.app.core.ports.in.comment;

import com.FrameHopper.app.core.domain.Comment;

public interface CreateCommentCommand {
    Comment CreateComment(Comment comment);
}
