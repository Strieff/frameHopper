package com.FrameHopper.app.View.Elements.Actions;

import com.FrameHopper.app.Model.Tag;
import com.FrameHopper.app.Model.Video;
import com.FrameHopper.app.View.Elements.DataManagers.TagListManager;

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
