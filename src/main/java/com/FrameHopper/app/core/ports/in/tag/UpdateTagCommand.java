package com.FrameHopper.app.core.ports.in.tag;

import com.FrameHopper.app.core.domain.Tag;

public interface UpdateTagCommand {
    Tag UpdateTag(Tag tag);
}
