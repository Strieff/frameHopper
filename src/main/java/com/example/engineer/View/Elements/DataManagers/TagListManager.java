package com.example.engineer.View.Elements.DataManagers;

import com.example.engineer.Model.Tag;
import com.example.engineer.Service.TagService;
import com.example.engineer.DBActions.DeleteTagAction;
import com.example.engineer.DBActions.HiddenStatusAction;
import com.example.engineer.DBActions.TagSettingsAction;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@Component
public class TagListManager {
    @Autowired
    TagService tagService;
    @Getter
    List<Tag> tagList;

    @PostConstruct
    public void init() {
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

    public void removeTag(int id){
        Tag t = getTag(id);

        tagList.remove(t);

        new DeleteTagAction(tagService,t).run();
    }

    public void removeTags(List<Tag> toDelete){
        tagList.removeAll(toDelete);

        new DeleteTagAction(tagService,toDelete).run();
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
        new TagSettingsAction(tagService,id,name,description,value).run();

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

        new HiddenStatusAction(tagService,id,hide).run();
    }
}
