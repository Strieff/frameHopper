package com.FrameHopper.app.core.ports.in.tag;

import com.FrameHopper.app.core.domain.Tag;

public interface CreateTagCommand {
    Tag CreateTag(Tag tag);
}
