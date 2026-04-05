package com.FrameHopper.app.core.ports.in.tag;

import com.FrameHopper.app.boundry.dto.TagDTO;
import com.FrameHopper.app.boundry.dto.VideoDTO;
import com.FrameHopper.app.core.domain.Tag;
import com.FrameHopper.app.core.domain.Video;

import java.util.List;

public interface TagsQuery {
    List<TagDTO> getAllTags();
    List<TagDTO> getAllOnVideo(VideoDTO video);
    List<Tag> getTagsOnVideoFrame(Video video, int frame);
    Tag getTagByName(String name);
    TagDTO getTagById(int id);
}
