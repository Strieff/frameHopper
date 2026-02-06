package com.FrameHopper.app.core.ports.in;

import com.FrameHopper.app.boundry.dto.VideoDTO;

import java.io.IOException;

public interface FrameBytesQuery {
    byte[] getVideoFrame(VideoDTO video, int index) throws IOException, InterruptedException;
}
