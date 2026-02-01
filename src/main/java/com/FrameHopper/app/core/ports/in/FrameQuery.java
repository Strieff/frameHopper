package com.FrameHopper.app.core.ports.in;

public interface FrameQuery {
    byte[] getVideoFrame(String path, int index);
}
