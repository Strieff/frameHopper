package com.FrameHopper.app.View.FXViews.VideoManagementList;

import com.FrameHopper.app.Model.Video;
import com.FrameHopper.app.Service.VideoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.stereotype.Component;

@Component
public class VideoManagementListService {
    private final VideoService videoService;

    public VideoManagementListService(VideoService videoService) {
        this.videoService = videoService;
    }

    public ObservableList<TableEntry> getAllVideos() {
        ObservableList<TableEntry> data = FXCollections.observableArrayList();

        videoService.getAll().stream()
                .map(v -> new TableEntry(v.getId(), v.getPath()))
                .forEach(data::add);

        return data;
    }

    public Video getVideo(int id) {
        return videoService.getById(id);
    }
}
