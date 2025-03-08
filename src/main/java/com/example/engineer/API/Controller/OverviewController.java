package com.example.engineer.API.Controller;

import com.example.engineer.API.Model.Overview.OverviewDTO;
import com.example.engineer.API.Model.Overview.TagOverviewDTO;
import com.example.engineer.API.Model.Overview.VideoOverviewDTO;
import com.example.engineer.Model.Video;
import com.example.engineer.Service.TagService;
import com.example.engineer.Service.VideoService;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/overview")
public class OverviewController {
    @Autowired
    private VideoService videoService;
    @Autowired
    private TagService tagService;

    @RequestMapping
    public ResponseEntity<String> getOverview() {
        var videos = videoService.getAllData();

        return collectData(videos);
    }

    @RequestMapping("/{video}")
    public ResponseEntity<String> getOverview(@PathVariable String video) {
        var videos = videoService.getAllData(video);

        return collectData(videos);
    }

    private ResponseEntity<String> collectData(List<Video> videos){
        if(videos.isEmpty())
            return ResponseEntity.notFound().build();

        var videoDTOs = videos.stream()
                .map(v -> new VideoOverviewDTO(
                        v,
                        videoService.getComplexity(v),
                        videoService.getTotalPoints(v)
                ))
                .toList();

        var tagDTOs = tagService.getAllTagData(videos)
                .entrySet().stream()
                .map(e -> new TagOverviewDTO(e.getKey(),e.getValue().getValue(),e.getValue().getKey()))
                .toList();

        return new ResponseEntity<>(new GsonBuilder().create().toJson(new OverviewDTO(videoDTOs,tagDTOs)), HttpStatus.OK);
    }
}
