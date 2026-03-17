package com.FrameHopper.app.core.ports.in.tag;

import com.FrameHopper.app.boundry.dto.TagDTO;

import java.util.List;

public interface CreateTagCommand {
    TagDTO CreateTag(TagDTO tag);
    List<TagDTO> CreateTags(List<TagDTO> tags);
}
