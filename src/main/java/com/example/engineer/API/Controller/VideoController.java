package com.example.engineer.API.Controller;

import com.example.engineer.API.Model.VideoDTO;
import com.example.engineer.Service.FrameService;
import com.example.engineer.Service.VideoService;
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
    public ResponseEntity<String> getAllVideos() throws JsonProcessingException {
        var videos = videoService.getAllData();

        if(videos.isEmpty())
            return ResponseEntity.notFound().build();

        var videoDTOs = videos.stream().map(VideoDTO::new).toList();

        return new ResponseEntity<>(new ObjectMapper().writeValueAsString(videoDTOs), HttpStatus.OK);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<String> getVideo(@PathVariable int id) throws JsonProcessingException {
        var video = videoService.getById(id);

        if(video == null)
            return ResponseEntity.notFound().build();

        frameService.getAllVideoData(video);

        var videoDTO = new VideoDTO(video);

        return new ResponseEntity<>(new ObjectMapper().writeValueAsString(videoDTO), HttpStatus.OK);
    }
}
