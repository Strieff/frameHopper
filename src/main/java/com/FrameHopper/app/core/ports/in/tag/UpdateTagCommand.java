package com.FrameHopper.app.core.ports.in.tag;

import com.FrameHopper.app.boundry.dto.TagDTO;

public interface UpdateTagCommand {
    TagDTO UpdateTag(TagDTO tag);
}
