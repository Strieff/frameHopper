package com.FrameHopper.app.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public class Comment {
    private final int id;
    @Setter
    private String content;
    private int listingOrder;
    private int videoId;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Comment comment)) return false;
        return id == comment.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
