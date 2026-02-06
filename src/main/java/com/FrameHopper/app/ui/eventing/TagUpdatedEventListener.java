package com.FrameHopper.app.ui.eventing;

import com.FrameHopper.app.boundry.dto.TagDTO;

public interface TagUpdatedEventListener {
    void onTagUpdated(TagDTO tagDTO);
    void onTagCreated(TagDTO tagDTO);
    void onTagDeleted(TagDTO tagDTO);
}
