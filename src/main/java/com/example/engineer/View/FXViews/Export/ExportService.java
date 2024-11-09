package com.example.engineer.View.FXViews.Export;

import com.example.engineer.Service.VideoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ExportService {
    @Autowired
    VideoService videoService;

    public ObservableList<TableEntry> getVideos(){
        var videoDTOList = videoService.getAll();
        ObservableList<TableEntry> data = FXCollections.observableArrayList();

        for(var video : videoDTOList){
            data.add(new TableEntry(
                    video.getId(),
                    video.getName()
            ));
        }

        return data;
    }

    public ObservableList<TableEntry> getVideos(Set<Integer> selectedIds) {
        var data = getVideos();
        data.forEach(e -> {
            if(selectedIds.contains(Integer.valueOf(e.getId())))
                e.setSelected(true);
        });

        return data;
    }

    public ObservableList<TableEntry> getFiltered(ObservableList<TableEntry> items, String text) {
        return items.filtered(item -> item.getName().toLowerCase().contains(text.toLowerCase()));
    }
}
