package com.FrameHopper.app.boundry.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CommentDTO{
    private int id;
    private String content;
    private int listingOrder;
    private int videoId;
}
