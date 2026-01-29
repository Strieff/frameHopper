package com.FrameHopper.app.API.Model.Overview;

import com.FrameHopper.app.Model.Tag;

public record TagOverviewDTO(
        String name,
        double value,
        int timesUsed,
        double totalPoints
) {
    public TagOverviewDTO(Tag tag, int timesUsed, double totalPoints) {
        this(
                tag.getName(),
                tag.getValue(),
                timesUsed,
                totalPoints
        );
    }
}
