package com.FrameHopper.app.core.application.analytics;

import com.FrameHopper.app.boundry.dto.VideoAnalyticsDTO;
import com.FrameHopper.app.boundry.dto.VideoDataDTO;

public interface VideoAnalyticsQuery {
    VideoAnalyticsDTO getComplexity(VideoDataDTO videoData);
    VideoAnalyticsDTO getUniqueTagsCount(VideoDataDTO videoData);
    VideoAnalyticsDTO getFrameCount(VideoDataDTO videoData);
    VideoAnalyticsDTO getRuntime(VideoDataDTO videoData);
    VideoAnalyticsDTO getTotalPoints(VideoDataDTO videoData);

}
