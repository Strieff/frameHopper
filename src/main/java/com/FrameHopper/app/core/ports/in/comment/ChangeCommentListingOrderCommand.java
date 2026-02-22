package com.FrameHopper.app.core.ports.in.comment;

import com.FrameHopper.app.boundry.dto.CommentDTO;

import java.util.List;

public interface ChangeCommentListingOrderCommand {
    List<CommentDTO> changeCommentListingOrder(List<CommentDTO> comments);
}
