package com.FrameHopper.app.core.ports.out.repository;

import com.FrameHopper.app.core.domain.Frame;
import com.FrameHopper.app.core.domain.Video;

import java.util.List;

public interface FrameRepositoryPort {
    List<Frame> getAllFramesOnVideo(Video video);
    Frame getFrameById(int id);
    List<Frame> getAll();
    Frame create(Frame frame);
    Frame update(Frame frame);
    void delete(int id);
}
