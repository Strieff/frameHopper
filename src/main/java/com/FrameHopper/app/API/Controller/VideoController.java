package com.FrameHopper.app.API.Controller;

import com.FrameHopper.app.API.Model.VideoDTO;
import com.FrameHopper.app.Service.FrameService;
import com.FrameHopper.app.Service.VideoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/video")
public class VideoController {
    private final VideoService videoService;
    private final FrameService frameService;

    public VideoController(VideoService videoService, FrameService frameService) {
        this.videoService = videoService;
        this.frameService = frameService;
    }

    @GetMapping
    public ResponseEntity<String> getAllVideos(@RequestParam boolean getNotes) throws JsonProcessingException {
        var videos = videoService.getAllData(getNotes);

        if(videos.isEmpty())
            return ResponseEntity.notFound().build();

        var videoDTOs = videos.stream().map(VideoDTO::new).toList();

        return new ResponseEntity<>(new ObjectMapper().writeValueAsString(videoDTOs), HttpStatus.OK);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<String> getVideo(@PathVariable int id, @RequestParam boolean getNotes) throws JsonProcessingException {
        var video = videoService.getById(id);

        if(video == null)
            return ResponseEntity.notFound().build();

        frameService.getAllVideoData(video, getNotes);

        var videoDTO = new VideoDTO(video);

        return new ResponseEntity<>(new ObjectMapper().writeValueAsString(videoDTO), HttpStatus.OK);
    }
}
