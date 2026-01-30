package com.FrameHopper.app.View.Elements.DataManagers;

import com.FrameHopper.app.Service.DataBaseManagementService;
import com.FrameHopper.app.Model.Tag;
import com.FrameHopper.app.Service.TagService;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@Component
public class TagListManager {
    private final TagService tagService;
    private final DataBaseManagementService dbService;

    @Getter
    private List<Tag> tagList;

    public TagListManager(TagService tagService, DataBaseManagementService dbService) {
        this.tagService = tagService;
        this.dbService = dbService;
        tagList = Collections.synchronizedList(tagService.getAllTags());
    }

    public Tag getTag(int id) {
        return tagList.stream()
                .filter(tag -> tag.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public Tag getTag(String name) {
        return tagList.stream()
                .filter(tag -> tag.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public int getSize(){
        return tagList.size();
    }

    private int getIndex(int id){
        return IntStream.range(0,tagList.size())
                .filter(i -> tagList.get(i).getId().equals(id))
                .findFirst()
                .orElse(-1);
    }

    public void removeTags(List<Tag> toDelete){
        tagList.removeAll(toDelete);

        dbService.deleteTags(toDelete);
    }

    public void addTag(String name, double value, String description){
        Tag t = tagService.createTag(name,value,description);
        tagList.add(t);
    }

    public void addTags(List<Tag> toAdd){
        var created = new ArrayList<Tag>();

        for(var t : toAdd)
            created.add(tagService.createTag(t.getName(),t.getValue(),t.getDescription()));

        tagList.addAll(created);
    }

    public void editTag(int id,String name, double value, String description,boolean hidden){
        dbService.editTag(id, name, description, value);

        tagList.set(getIndex(id), Tag.builder()
                        .id(id)
                        .name(name)
                        .value(value)
                        .description(description)
                        .deleted(hidden)
                        .build());
    }

    public void changeHideStatus(int id, boolean hide){
        tagList.get(getIndex(id)).setDeleted(hide);

        dbService.hiddenStatusChange(id, hide);
    }
}
