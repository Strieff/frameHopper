package com.FrameHopper.app.core.application.analytics;

import com.FrameHopper.app.boundry.dto.FrameDTO;
import com.FrameHopper.app.boundry.dto.TagDTO;
import com.FrameHopper.app.boundry.dto.analytics.TagAnalyticsDTO;
import com.FrameHopper.app.boundry.dto.analytics.TagAnalyticsParameterDTO;
import com.FrameHopper.app.boundry.dto.analytics.TagDataAnalyticsDTO;
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

    @Override
    public TagDataAnalyticsDTO getAnalytics(List<TagDTO> tags, List<VideoDataDTO> videoData) {
        if (tags.isEmpty() || videoData.isEmpty()) return null;

        var data = tags.stream().map(td -> new TagAnalyticsDTO(
                td.getName(),
                td.getValue(),
                getAmountUsed(td, videoData).data().intValue(),
                getTotalPoints(td, videoData).data().doubleValue()
        )).toList();

        return new TagDataAnalyticsDTO(
                data,
                data.size(),
                data.stream().mapToDouble(TagAnalyticsDTO::totalPoints).sum()
        );
    }
}
