package com.example.engineer.View.Elements.Actions;

import com.example.engineer.Model.Tag;
import com.example.engineer.Model.Video;
import com.example.engineer.View.Elements.DataManagers.TagListManager;

import java.util.List;


public  class ActionHandler {
    protected List<Integer> tagIdList;
    protected TagListManager tagList;

    public void addTag(Integer id){
        tagIdList.add(id);
    }

    public void performAction(List<Tag> existingTags,int currentFrameIndex, Video video){

    }

    protected List<Tag> getTagList(){
        return tagList.getTagList().stream()
                .filter(t -> tagIdList.contains(t.getId()))
                .toList();
    }


    protected List<Tag> filterTagList(List<Tag> existingTags){
        return null;
    }


}
