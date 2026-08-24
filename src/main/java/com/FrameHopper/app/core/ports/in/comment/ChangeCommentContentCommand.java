package com.FrameHopper.app.core.ports.in.comment;

import com.FrameHopper.app.boundry.dto.CommentDTO;

public interface ChangeCommentContentCommand {
    CommentDTO updateCommentContent(CommentDTO comment);
}
