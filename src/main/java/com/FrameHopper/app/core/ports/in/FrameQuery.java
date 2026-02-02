package com.FrameHopper.app.core.ports.in;

import com.FrameHopper.app.core.domain.Video;

import java.io.IOException;

public interface FrameQuery {
    byte[] getVideoFrame(Video video, int index) throws IOException, InterruptedException;
}
