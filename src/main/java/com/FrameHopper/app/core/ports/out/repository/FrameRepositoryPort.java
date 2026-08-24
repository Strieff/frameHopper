package com.FrameHopper.app.core.ports.out.repository;

import com.FrameHopper.app.core.domain.Frame;
import com.FrameHopper.app.core.domain.Video;

import java.util.List;

public interface FrameRepositoryPort {
    List<Frame> getAllFramesOnVideo(Video video);
    List<Frame> getAllFramesOnVideos(List<Video> videos);
    Frame getFrameById(int id);
    Frame getFrameByVideoAndFrameNumber(Video video, int frameNumber);
    List<Frame> getAll();
    Frame create(Frame frame);
    Frame update(Frame frame);
    void delete(int id);
}
