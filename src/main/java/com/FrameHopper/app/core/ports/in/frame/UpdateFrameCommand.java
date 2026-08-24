package com.FrameHopper.app.core.ports.in.frame;

import com.FrameHopper.app.boundry.dto.FrameDTO;

public interface UpdateFrameCommand {
    FrameDTO updateFrame(FrameDTO frame);
}
