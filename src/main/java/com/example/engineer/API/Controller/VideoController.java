package com.example.engineer.API.Controller;

import com.example.engineer.API.Model.VideoDTO;
import com.example.engineer.Service.FrameService;
import com.example.engineer.Service.VideoService;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/video")
public class VideoController {
    @Autowired
    VideoService videoService;
    @Autowired
    FrameService frameService;

    @GetMapping
    public ResponseEntity<String> getAllVideos() {
        var videos = videoService.getAllData();

        if(videos.isEmpty())
            return ResponseEntity.notFound().build();

        var videoDTOs = videos.stream().map(VideoDTO::new).toList();

        return new ResponseEntity<>(new GsonBuilder().create().toJson(videoDTOs), HttpStatus.OK);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<String> getVideo(@PathVariable String name) {
        var videos = videoService.getAllByName(name.replace("%20"," "));

        if(videos.isEmpty())
            return ResponseEntity.notFound().build();

        videos.forEach(v -> frameService.getAllVideoData(v));

        var videoDTOs = videos.stream().map(VideoDTO::new).toList();

        if(videoDTOs.size() == 1)
            return ResponseEntity.status(HttpStatus.OK).body(videoDTOs.getFirst().toString());

        return new ResponseEntity<>(new GsonBuilder().create().toJson(videoDTOs), HttpStatus.OK);
    }
}
