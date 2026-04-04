package com.FrameHopper.app.core.ports.in.tag;

import com.FrameHopper.app.boundry.dto.TagDTO;
import com.FrameHopper.app.core.domain.Tag;
import com.FrameHopper.app.core.domain.Video;

import java.util.List;

public interface TagsQuery {
    List<TagDTO> getAllTags();
    List<Tag> getTagsOnVideoFrame(Video video, int frame);
    Tag getTagByName(String name);
    Tag getTagById(int id);
}
