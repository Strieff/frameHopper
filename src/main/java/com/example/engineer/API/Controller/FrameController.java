package com.example.engineer.API.Controller;

import com.example.engineer.API.Model.FrameDTO;
import com.example.engineer.Service.FrameService;
import com.example.engineer.Service.VideoService;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/frame")
public class FrameController {
    @Autowired
    private FrameService frameService;
    @Autowired
    private VideoService videoService;

    @GetMapping("id/{id}")
    public ResponseEntity<String> getFrameById(@PathVariable int id){
        var frame = frameService.getById(id);

        if(frame == null)
            return ResponseEntity.notFound().build();

        var frameDTO = new FrameDTO(frame);

        return new ResponseEntity<>(new GsonBuilder().create().toJson(frameDTO), HttpStatus.OK);
    }

    @GetMapping("video/{videoName}")
    public ResponseEntity<String> getFramesByVideoName(@PathVariable String videoName){
        if(!videoService.existsByName(videoName))
            return ResponseEntity.notFound().build();

        var videos = videoService.getAllByName(videoName);

        if(videos.size() == 1) {
            var frames = frameService.getAllByVideo(videos.getFirst()).stream()
                    .map(FrameDTO::new)
                    .toList();

            return new ResponseEntity<>(new GsonBuilder().create().toJson(frames), HttpStatus.OK);
        }

        var framesDTOs = videos.stream().map(v -> frameService.getAllByVideo(v).stream()
                    .map(FrameDTO::new)
                    .toList()
                )
                .toList();

        return new ResponseEntity<>(new GsonBuilder().create().toJson(framesDTOs), HttpStatus.OK);
    }

    @GetMapping("video/{videoName}/frame/{frameNo}")
    public ResponseEntity<String> getFrameOfVideo(@PathVariable String videoName, @PathVariable int frameNo){
        var videos = videoService.getAllByName(videoName);

        if(videos.isEmpty())
            return ResponseEntity.notFound().build();

        if(videos.size() == 1){
            var frame = frameService.getFrame(videos.getFirst(), frameNo);

            if(frame == null)
                return ResponseEntity.notFound().build();

            var frameDTO = new FrameDTO(frame);

            return new ResponseEntity<>(new GsonBuilder().create().toJson(frameDTO), HttpStatus.OK);
        }

        var frameDTOs = videos.stream()
                .map(v -> frameService.getFrame(v, frameNo))
                .map(FrameDTO::new)
                .toList();

        return new ResponseEntity<>(new GsonBuilder().create().toJson(frameDTOs), HttpStatus.OK);
    }
}
