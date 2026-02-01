package com.FrameHopper.app.core.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Comment {
    private final int id;
    @Setter
    private String content;
    private int listingOrder;

    public Comment(int id, String content, int listingOrder) {
        this.id = id;
        this.content = content;
        this.listingOrder = listingOrder;
    }

    public void changeListingOrder(int listingOrder) {
        if(listingOrder < 0)
            throw new IllegalArgumentException("Listing order cannot be negative");
        this.listingOrder = listingOrder;
    }
}
