package com.FrameHopper.app.core.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.List;
import java.util.Objects;

@Getter
public class Video {
    private final int id;
    private String name;
    private String path;
    @Setter
    private VideoMetadata metadata;
    private final List<Comment> notes;

    public Video(
            int id,
            String path,
            String name,
            VideoMetadata metadata,
            List<Comment> notes
    ) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.metadata = metadata;
        this.notes = notes;
    }

    public Video(
            int id,
            String name,
            String path,
            List<Comment> notes
    ) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.metadata = null;
        this.notes = notes;
    }

    public void changePath(String path) {
        this.path = path;
        if(!new File(path).getName().equals(this.name))
            this.name = new File(path).getName();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Video video)) return false;
        return id == video.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public record VideoMetadata(int totalFrames, double frameRate, double duration, int height, int width) {}
}
