package com.example.engineer.API.Model.Overview;

import com.example.engineer.Model.Tag;

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
