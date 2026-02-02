package com.FrameHopper.app.core.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
public class Frame {
    @Setter
    int id;
    int frameNumber;
    @Setter
    Video video;
    @Setter
    List<Tag> tags;

    public void setFrameNumber(int frameNumber) {
        if(frameNumber < 0)
            throw new IllegalArgumentException("Invalid frame number");

        this.frameNumber = frameNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Frame frame)) return false;
        return id == frame.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
