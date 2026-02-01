package com.FrameHopper.app.core.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.File;

@Getter
public class Video {
    private final int id;
    private String name;
    private String path;
    @Setter
    private VideoMetadata metadata;

    public Video(
            int id,
            String name,
            String path,
            VideoMetadata metadata
    ) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.metadata = metadata;
    }

    public Video(
            int id,
            String name,
            String path
    ) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.metadata = null;
    }

    public void changePath(String path) {
        this.path = path;
        if(!new File(path).getName().equals(this.name))
            this.name = new File(path).getName();
    }

    public record VideoMetadata(int totalFrames, double frameRate, double duration, int height, int width) {}
}
