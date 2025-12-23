package com.example.engineer.API.Controller;

import com.example.engineer.API.Model.Tag.TagDTO;
import com.example.engineer.Service.TagService;
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
@RequestMapping("api/code")
public class TagController {
    private final TagService tagService;
    private final VideoService videoService;

    public TagController(TagService tagService, VideoService videoService) {
        this.tagService = tagService;
        this.videoService = videoService;
    }

    @GetMapping
    public ResponseEntity<String> getAll() throws JsonProcessingException {
        var tags = tagService.getAllEnriched();

        if(tags.isEmpty())
            return ResponseEntity.noContent().build();

        var tagDTOs = tags.stream().map(TagDTO::new).toList();

        return new ResponseEntity<>(new ObjectMapper().writeValueAsString(tagDTOs), HttpStatus.OK);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<String> getById(@PathVariable int id) throws JsonProcessingException {
        var tag = tagService.getById(id);

        if (tag == null)
            return ResponseEntity.notFound().build();

        var tagDTO = new TagDTO(tag);

        return new ResponseEntity<>(new ObjectMapper().writeValueAsString(tagDTO), HttpStatus.OK);
    }

    @GetMapping("name/{name}")
    public ResponseEntity<String> getByName(@PathVariable String name) throws JsonProcessingException {
        var tag = tagService.getTag(name);

        if(tag == null)
            return ResponseEntity.notFound().build();

        var tagDTO = new TagDTO(tag);

        return new ResponseEntity<>(new ObjectMapper().writeValueAsString(tagDTO), HttpStatus.OK);
    }

    @GetMapping("/video/{id}")
    public ResponseEntity<String> getUsedOnVideo(@PathVariable int id) throws JsonProcessingException {
        if (!videoService.exists(id))
            return ResponseEntity.notFound().build();

        var videos = videoService.getById(id);

        var tagDTOs = tagService.getAllEnriched(videos).stream()
                .map(TagDTO::new)
                .toList();

        return new ResponseEntity<>(new ObjectMapper().writeValueAsString(tagDTOs), HttpStatus.OK);
    }
}
