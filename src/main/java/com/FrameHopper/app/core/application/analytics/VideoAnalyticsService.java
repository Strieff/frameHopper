package com.FrameHopper.app.core.application.analytics;

import com.FrameHopper.app.boundry.dto.FrameDTO;
import com.FrameHopper.app.boundry.dto.TagDTO;
import com.FrameHopper.app.boundry.dto.VideoAnalyticsDTO;
import com.FrameHopper.app.boundry.dto.VideoDataDTO;

import java.util.List;
import java.util.stream.Collectors;

public class VideoAnalyticsService implements VideoAnalyticsQuery{
    @Override
    public VideoAnalyticsDTO getComplexity(VideoDataDTO videoData) {
        if(videoData.frames().isEmpty()) return new VideoAnalyticsDTO(videoData.video(), 0);

        double totalPoints = videoData.frames().stream()
                    .map(FrameDTO::tags)
                    .flatMap(List::stream)
                    .mapToDouble(TagDTO::getValue)
                    .sum();

        double raw = totalPoints / videoData.video().metadata().duration();
        double complexity = Math.round(raw * 1000.0) / 1000.0;

        return new VideoAnalyticsDTO(videoData.video(), complexity);
    }

    @Override
    public VideoAnalyticsDTO getUniqueTagsCount(VideoDataDTO videoData) {
        double uniqueTags = 0;

        if(!videoData.frames().isEmpty())
            uniqueTags = videoData.frames().stream()
                    .map(FrameDTO::tags)
                    .flatMap(List::stream)
                    .collect(Collectors.toSet())
                    .size();

        return new VideoAnalyticsDTO(
                videoData.video(),
                uniqueTags
        );
    }

    @Override
    public VideoAnalyticsDTO getFrameCount(VideoDataDTO videoData) {
        return new VideoAnalyticsDTO(
                videoData.video(),
                videoData.video().metadata().totalFrames()
        );
    }

    @Override
    public VideoAnalyticsDTO getRuntime(VideoDataDTO videoData) {
        return new  VideoAnalyticsDTO(
                videoData.video(),
                videoData.video().metadata().duration()
        );
    }

    @Override
    public VideoAnalyticsDTO getTotalPoints(VideoDataDTO videoData) {
        double totalPoints = 0;

        if(!videoData.frames().isEmpty())
            totalPoints = videoData.frames().stream()
                    .map(FrameDTO::tags)
                    .flatMap(List::stream)
                    .mapToDouble(TagDTO::getValue)
                    .sum();

        return new VideoAnalyticsDTO(
                videoData.video(),
                totalPoints
        );
    }
}
