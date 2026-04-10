package com.FrameHopper.app.core.ports.in;

import com.FrameHopper.app.boundry.dto.VideoDTO;

import java.io.IOException;

public interface FrameBytesQuery {
    byte[] getVideoFrame(VideoDTO video, int index) throws IOException, InterruptedException;
    byte[] rotateFrame(VideoDTO video, int index, int angle) throws IOException, InterruptedException;
    byte[] flipFrame(VideoDTO video, int index, boolean horizontal) throws IOException, InterruptedException;
}
