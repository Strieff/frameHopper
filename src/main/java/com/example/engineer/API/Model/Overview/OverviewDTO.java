package com.example.engineer.API.Model.Overview;

import com.google.gson.GsonBuilder;

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

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
}
