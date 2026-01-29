package com.FrameHopper.app.API.Model.Overview;

import java.util.Collections;
import java.util.List;

public record OverviewDTO(
        List<VideoOverviewDTO> videoDTOs,
        List<TagOverviewDTO> tagDTOs
) {
    public OverviewDTO{
        videoDTOs = (videoDTOs != null) ? videoDTOs : Collections.emptyList();
        tagDTOs = (tagDTOs != null) ? tagDTOs : Collections.emptyList();
    }
}
