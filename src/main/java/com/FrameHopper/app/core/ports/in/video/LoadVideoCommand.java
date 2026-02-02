package com.FrameHopper.app.core.ports.in.video;

import com.FrameHopper.app.core.domain.Video;

import java.io.IOException;

public interface LoadVideoCommand {
    Video loadVideo(String path) throws IOException, InterruptedException;
    Video loadVideo(int id) throws IOException, InterruptedException;
}
