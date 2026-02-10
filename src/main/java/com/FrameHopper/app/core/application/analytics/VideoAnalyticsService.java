package com.FrameHopper.app.core.application.analytics;

import com.FrameHopper.app.boundry.dto.FrameDTO;
import com.FrameHopper.app.boundry.dto.TagDTO;
import com.FrameHopper.app.boundry.dto.analytics.VideoDataAnalyticsDTO;
import com.FrameHopper.app.boundry.dto.analytics.VideoAnalyticsParameterDTO;
import com.FrameHopper.app.boundry.dto.analytics.VideoDataDTO;

import java.util.List;
import java.util.stream.Collectors;

public class VideoAnalyticsService implements VideoAnalyticsQuery {
    @Override
    public VideoAnalyticsParameterDTO getComplexity(VideoDataDTO videoData) {
        if(videoData.frames().isEmpty()) return new VideoAnalyticsParameterDTO(videoData.video(), 0);

        double totalPoints = videoData.frames().stream()
                    .map(FrameDTO::tags)
                    .flatMap(List::stream)
                    .mapToDouble(TagDTO::getValue)
                    .sum();

        double raw = totalPoints / videoData.video().metadata().duration();
        double complexity = Math.round(raw * 1000.0) / 1000.0;

        return new VideoAnalyticsParameterDTO(videoData.video(), complexity);
    }

    @Override
    public VideoAnalyticsParameterDTO getUniqueTagsCount(VideoDataDTO videoData) {
        double uniqueTags = 0;

        if(!videoData.frames().isEmpty())
            uniqueTags = videoData.frames().stream()
                    .map(FrameDTO::tags)
                    .flatMap(List::stream)
                    .collect(Collectors.toSet())
                    .size();

        return new VideoAnalyticsParameterDTO(
                videoData.video(),
                uniqueTags
        );
    }

    @Override
    public VideoAnalyticsParameterDTO getFrameCount(VideoDataDTO videoData) {
        return new VideoAnalyticsParameterDTO(
                videoData.video(),
                videoData.video().metadata().totalFrames()
        );
    }

    @Override
    public VideoAnalyticsParameterDTO getRuntime(VideoDataDTO videoData) {
        return new VideoAnalyticsParameterDTO(
                videoData.video(),
                videoData.video().metadata().duration()
        );
    }

    @Override
    public VideoAnalyticsParameterDTO getTotalPoints(VideoDataDTO videoData) {
        double totalPoints = 0;

        if(!videoData.frames().isEmpty())
            totalPoints = videoData.frames().stream()
                    .map(FrameDTO::tags)
                    .flatMap(List::stream)
                    .mapToDouble(TagDTO::getValue)
                    .sum();

        return new VideoAnalyticsParameterDTO(
                videoData.video(),
                totalPoints
        );
    }

    @Override
    public double getASL(List<VideoDataDTO> videoData) {
        return 0;
    }

    @Override
    public VideoDataAnalyticsDTO getAnalytics(List<VideoDataDTO> videoData) {
        return null;
    }
}
