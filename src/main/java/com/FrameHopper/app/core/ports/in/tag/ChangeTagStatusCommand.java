package com.FrameHopper.app.core.ports.in.tag;

import java.util.List;

public interface ChangeTagStatusCommand {
    void ChangeTagStatus(int id);
    void ChangeTagStatus(List<Integer> ids);
}
