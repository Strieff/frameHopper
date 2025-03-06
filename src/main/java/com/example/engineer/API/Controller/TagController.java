package com.example.engineer.API.Controller;

import com.example.engineer.API.Model.Tag.TagDTO;
import com.example.engineer.Service.FrameService;
import com.example.engineer.Service.TagService;
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
@RequestMapping("api/code")
public class TagController {
    @Autowired
    private TagService tagService;
    @Autowired
    private VideoService videoService;
    @Autowired
    private FrameService frameService;

    @GetMapping
    public ResponseEntity<String> getAll(){
        var tags = tagService.getAllEnriched();

        if(tags.isEmpty())
            return ResponseEntity.noContent().build();

        var tagDTOs = tags.stream().map(TagDTO::new).toList();

        return new ResponseEntity<>(new GsonBuilder().create().toJson(tagDTOs), HttpStatus.OK);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<String> getById(@PathVariable("id") int id){
        var tag = tagService.getById(id);

        if (tag == null)
            return ResponseEntity.notFound().build();

        var tagDTO = new TagDTO(tag);

        return new ResponseEntity<>(new GsonBuilder().create().toJson(tagDTO), HttpStatus.OK);
    }

    @GetMapping("name/{name}")
    public ResponseEntity<String> getByName(@PathVariable("name") String name){
        var tag = tagService.getTag(name);

        if(tag == null)
            return ResponseEntity.notFound().build();

        var tagDTO = new TagDTO(tag);

        return new ResponseEntity<>(new GsonBuilder().create().toJson(tagDTO), HttpStatus.OK);
    }

    @GetMapping("/video/{video}")
    public ResponseEntity<String> getUsedOnVideo(@PathVariable("video") String video){
        var videos = videoService.getAllByName(video);

        if(videos.isEmpty())
            return ResponseEntity.notFound().build();

        if(videos.size() == 1){
            var tagDTOs = tagService.getAllEnriched(videos.getFirst()).stream()
                    .map(TagDTO::new)
                    .toList();

            return new ResponseEntity<>(new GsonBuilder().create().toJson(tagDTOs), HttpStatus.OK);
        }

        return null;
    }
}
