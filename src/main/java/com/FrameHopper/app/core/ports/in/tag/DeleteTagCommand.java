package com.FrameHopper.app.core.ports.in.tag;

import java.util.List;

public interface DeleteTagCommand {
    void deleteTag(int id);
    void deleteTags(List<Integer> ids);
}
