package com.FrameHopper.app.API.Controller;

import com.FrameHopper.app.API.Model.Overview.OverviewDTO;
import com.FrameHopper.app.API.Model.Overview.TagOverviewDTO;
import com.FrameHopper.app.API.Model.Overview.VideoOverviewDTO;
import com.FrameHopper.app.Model.Video;
import com.FrameHopper.app.Service.TagService;
import com.FrameHopper.app.Service.VideoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/overview")
public class OverviewController {
    private final VideoService videoService;
    private final TagService tagService;

    public OverviewController(VideoService videoService, TagService tagService) {
        this.videoService = videoService;
        this.tagService = tagService;
    }

    @RequestMapping
    public ResponseEntity<String> getOverview() throws JsonProcessingException {
        var videos = videoService.getAllData(false);

        return collectData(videos);
    }

    @RequestMapping("/{id}")
    public ResponseEntity<String> getOverview(@PathVariable int id) throws JsonProcessingException {
        if (!videoService.exists(id))
            return ResponseEntity.notFound().build();

        var videos = videoService.getVideoData(id, false);

        return collectData(videos);
    }

    private ResponseEntity<String> collectData(List<Video> videos) throws JsonProcessingException {
        if(videos.isEmpty())
            return ResponseEntity.notFound().build();

        var videoDTOs = videos.stream()
                .map(v -> new VideoOverviewDTO(
                        v,
                        videoService.getTotalPoints(v),
                        videoService.getComplexity(v)
                ))
                .toList();

        var tagDTOs = tagService.getAllTagData(videos)
                .entrySet().stream()
                .map(e -> new TagOverviewDTO(e.getKey(),e.getValue().getValue(),e.getValue().getKey()))
                .toList();

        return new ResponseEntity<>(new ObjectMapper().writeValueAsString(new OverviewDTO(videoDTOs,tagDTOs)), HttpStatus.OK);
    }
}
