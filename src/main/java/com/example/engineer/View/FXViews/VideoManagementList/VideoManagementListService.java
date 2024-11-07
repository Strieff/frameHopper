package com.example.engineer.View.FXViews.VideoManagementList;

import com.example.engineer.Model.Video;
import com.example.engineer.Service.VideoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VideoManagementListService {
    @Autowired
    private VideoService videoService;

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
