package com.FrameHopper.app.ui.eventing;

import com.FrameHopper.app.boundry.dto.FrameDTO;

public interface FrameUpdatedListener {
    void onFrameUpdate(int index, FrameDTO frame);
}
