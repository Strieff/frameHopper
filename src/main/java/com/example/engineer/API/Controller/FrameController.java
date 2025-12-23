package com.example.engineer.API.Controller;

import com.example.engineer.API.Model.FrameDTO;
import com.example.engineer.Service.FrameService;
import com.example.engineer.Service.VideoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/frame")
public class FrameController {
    private final FrameService frameService;
    private final VideoService videoService;

    public FrameController(FrameService frameService, VideoService videoService) {
        this.frameService = frameService;
        this.videoService = videoService;
    }

    @GetMapping("id/{id}")
    public ResponseEntity<String> getFrameById(@PathVariable int id) throws JsonProcessingException {
        var frame = frameService.getById(id);

        if(frame == null)
            return ResponseEntity.notFound().build();

        var frameDTO = new FrameDTO(frame);

        return new ResponseEntity<>(new ObjectMapper().writeValueAsString(frameDTO), HttpStatus.OK);
    }

    @GetMapping("video/{id}")
    public ResponseEntity<String> getFramesByVideoName(@PathVariable int id) throws JsonProcessingException {
        if(!videoService.exists(id))
            return ResponseEntity.notFound().build();

        var video = videoService.getById(id);

        return new ResponseEntity<>(
                new ObjectMapper().writeValueAsString(
                        frameService.getAllByVideo(video).stream()
                                .map(FrameDTO::new)
                                .toList()
                        ),
                HttpStatus.OK
        );
    }

    @GetMapping("video/{videoId}/frame/{frameNo}")
    public ResponseEntity<String> getFrameOfVideo(@PathVariable int videoId, @PathVariable int frameNo) throws JsonProcessingException {
        if (!videoService.exists(videoId))
            return ResponseEntity.notFound().build();

        var video = videoService.getById(videoId);

        var frame = frameService.getFrame(video, frameNo);

        if (frame == null)
            return ResponseEntity.notFound().build();

        var frameDTO = new FrameDTO(frame);

        return new ResponseEntity<>(new ObjectMapper().writeValueAsString(frameDTO), HttpStatus.OK);

    }
}
