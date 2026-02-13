package com.FrameHopper.app.core.application.analytics;

import com.FrameHopper.app.boundry.dto.analytics.VideoDataAnalyticsDTO;
import com.FrameHopper.app.boundry.dto.analytics.VideoAnalyticsParameterDTO;
import com.FrameHopper.app.boundry.dto.analytics.VideoDataDTO;

import java.util.List;

public interface VideoAnalyticsQuery {
    VideoAnalyticsParameterDTO getComplexity(VideoDataDTO videoData);
    VideoAnalyticsParameterDTO getUniqueTagsCount(VideoDataDTO videoData);
    VideoAnalyticsParameterDTO getFrameCount(VideoDataDTO videoData);
    VideoAnalyticsParameterDTO getRuntime(VideoDataDTO videoData);
    VideoAnalyticsParameterDTO getTotalPoints(VideoDataDTO videoData);
    VideoAnalyticsParameterDTO getFrameRate(VideoDataDTO videoData);
    double getASL(List<VideoDataDTO> videoData);
    VideoDataAnalyticsDTO getAnalytics(List<VideoDataDTO> videoData);
}
