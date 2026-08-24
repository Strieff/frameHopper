package com.FrameHopper.app.core.ports.in.tag;

import com.FrameHopper.app.boundry.dto.TagDTO;
import com.FrameHopper.app.boundry.dto.VideoDTO;

import java.util.List;

public interface TagsQuery {
    List<TagDTO> getAllTags();
    List<TagDTO> getAllOnVideo(VideoDTO video);
    List<TagDTO> getTagsOnVideoFrame(VideoDTO videoDTO, int frame);
    TagDTO getTagByName(String name);
    TagDTO getTagById(int id);
    List<TagDTO> getTagsByIds(List<Integer> ids);
}
