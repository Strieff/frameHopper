package com.FrameHopper.app.core.ports.in.tag;

import com.FrameHopper.app.core.domain.Tag;

import java.util.List;

public interface TagsQuery {
    List<Tag> getAllTags();
    List<Tag> getAllVisible();
    Tag getTagByName(String name);
    Tag getTagById(int id);
}
