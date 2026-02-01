package com.FrameHopper.app.core.application.video;

import com.FrameHopper.app.core.domain.Video;
import com.FrameHopper.app.core.ports.in.video.CreateVideoCommand;
import com.FrameHopper.app.core.ports.in.video.DeleteVideoCommand;
import com.FrameHopper.app.core.ports.in.video.UpdateVideoPathCommand;

public class VideoCommandService implements
        DeleteVideoCommand,
        UpdateVideoPathCommand,
        CreateVideoCommand {
    @Override
    public void deleteVideo(Video video) {

    }

    @Override
    public void updateVideoPath(Video video, String path) {

    }

    @Override
    public Video createVideo(Video video) {
        return null;
    }
}
