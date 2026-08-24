package com.FrameHopper.app.core.application.analytics;

import com.FrameHopper.app.boundry.dto.TagDTO;
import com.FrameHopper.app.boundry.dto.analytics.TagAnalyticsParameterDTO;
import com.FrameHopper.app.boundry.dto.analytics.TagDataAnalyticsDTO;
import com.FrameHopper.app.boundry.dto.analytics.VideoDataDTO;

import java.util.List;

public interface TagAnalyticsQuery {
    TagAnalyticsParameterDTO getValue(TagDTO tag);
    TagAnalyticsParameterDTO getAmountUsed(TagDTO tag, List<VideoDataDTO> videos);
    TagAnalyticsParameterDTO getTotalPoints(TagDTO tag, List<VideoDataDTO> videos);
    TagDataAnalyticsDTO getAnalytics(List<TagDTO> tags, List<VideoDataDTO> videoData);
}
