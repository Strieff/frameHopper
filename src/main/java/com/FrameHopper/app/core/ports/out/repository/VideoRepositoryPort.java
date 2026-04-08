package com.FrameHopper.app.core.ports.out.repository;

import com.FrameHopper.app.core.domain.Video;

import java.util.List;

public interface VideoRepositoryPort {
    Video getById(int id);
    Video getByPath(String path);
    List<Video> getByName(String name);
    List<Video> getAll();
    List<Video> getAllWithNotes();
    Video create(Video video);
    Video update(Video video);
    void delete(int id);
}
