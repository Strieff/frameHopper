package com.FrameHopper.app.core.ports.out.repository;

import com.FrameHopper.app.core.domain.Tag;
import com.FrameHopper.app.core.domain.Video;

import java.util.List;

public interface TagRepositoryPort {
    Tag getById(int id);
    Tag getByName(String name);
    List<Tag> getAllByVideo(Video video);
    List<Tag> getTagsOnVideoFrame(Video video, int frame);
    List<Tag> getAll();
    Tag create(Tag tag);
    List<Tag> create(List<Tag> tags);
    Tag update(Tag tag);
    void updateStatus(int id);
    void updateStatus(List<Integer> id);
    void delete(int id);
    void delete(List<Integer> ids);
}
