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

    public void changeListingOrder(int listingOrder) {
        if(listingOrder < 0)
            throw new IllegalArgumentException("Listing order cannot be negative");
        this.listingOrder = listingOrder;
    }

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
