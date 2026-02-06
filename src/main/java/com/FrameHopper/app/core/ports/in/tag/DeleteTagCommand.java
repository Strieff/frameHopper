package com.FrameHopper.app.core.ports.in.tag;

import java.util.List;

public interface DeleteTagCommand {
    void DeleteTag(int id);
    void DeleteTags(List<Integer> ids);
}
