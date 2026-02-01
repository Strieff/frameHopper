package com.FrameHopper.app.core.ports.in.tag;

import com.FrameHopper.app.core.domain.Tag;

public interface ChangeTagStatusCommand {
    Tag ChangeTagStatus(int id);
}
