package com.FrameHopper.app.View.FXViews.VideoManagementList;

import com.FrameHopper.app.Service.VideoService;
import com.FrameHopper.app.settings.UserSettingsService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VideoManagementListService {
    private final VideoService videoService;
    private final UserSettingsService userSettingsService;

    public VideoManagementListService(
            VideoService videoService,
            UserSettingsService userSettingsService
    ) {
        this.videoService = videoService;
        this.userSettingsService = userSettingsService;
    }

    public List<TableEntry> getAllVideos() {
        return videoService.getAll().stream()
                .map(TableEntry::new)
                .toList();
    }

    public void deleteVideo(int id) {
        videoService.deleteVideo(id);

        if(userSettingsService.getRecentId() == id)
            userSettingsService.setRecentId(-1);
    }
}
