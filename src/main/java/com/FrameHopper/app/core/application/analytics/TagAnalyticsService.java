package com.FrameHopper.app.core.application.analytics;

import com.FrameHopper.app.boundry.dto.FrameDTO;
import com.FrameHopper.app.boundry.dto.TagDTO;
import com.FrameHopper.app.boundry.dto.analytics.TagAnalyticsParameterDTO;
import com.FrameHopper.app.boundry.dto.analytics.VideoDataDTO;

import java.util.List;

public class TagAnalyticsService implements TagAnalyticsQuery {
    @Override
    public TagAnalyticsParameterDTO getValue(TagDTO tag) {
        return new TagAnalyticsParameterDTO(tag, tag.getValue());
    }

    @Override
    public TagAnalyticsParameterDTO getAmountUsed(TagDTO tag, List<VideoDataDTO> videoData) {
        if(videoData == null || videoData.isEmpty())
            return new TagAnalyticsParameterDTO(tag, 0);

        var data = videoData.stream()
                .map(VideoDataDTO::frames)
                .flatMap(List::stream)
                .map(FrameDTO::tags)
                .flatMap(List::stream)
                .filter(t -> t.getId() == tag.getId())
                .count();

        return new TagAnalyticsParameterDTO(tag, data);
    }

    @Override
    public TagAnalyticsParameterDTO getTotalPoints(TagDTO tag, List<VideoDataDTO> videoData) {
        if(videoData == null || videoData.isEmpty())
            return new TagAnalyticsParameterDTO(tag, 0);

        var data = videoData.stream()
                .map(VideoDataDTO::frames)
                .flatMap(List::stream)
                .map(FrameDTO::tags)
                .flatMap(List::stream)
                .filter(t -> t.getId() == tag.getId())
                .mapToDouble(TagDTO::getValue)
                .sum();

        return new TagAnalyticsParameterDTO(tag, data);
    }
}
