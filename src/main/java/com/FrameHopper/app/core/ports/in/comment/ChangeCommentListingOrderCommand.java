package com.FrameHopper.app.core.ports.in.comment;

import com.FrameHopper.app.core.domain.Comment;

public interface ChangeCommentListingOrderCommand {
    void changeCommentListingOrder(Comment comment, int order);
}
