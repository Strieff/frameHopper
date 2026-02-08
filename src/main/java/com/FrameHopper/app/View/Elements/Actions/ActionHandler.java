package com.FrameHopper.app.View.Elements.Actions;

import com.FrameHopper.app.Model.Tag;
import com.FrameHopper.app.Model.Video;

import java.util.List;


public  class ActionHandler {
    protected List<Integer> tagIdList;

    public void addTag(Integer id){
        tagIdList.add(id);
    }

    public void performAction(List<Tag> existingTags,int currentFrameIndex, Video video){

    }

    protected List<Tag> getTagList(){
        /*return tagList.getTagList().stream()
                .filter(t -> tagIdList.contains(t.getId()))
                .toList();*/

        return null;
    }


    protected List<Tag> filterTagList(List<Tag> existingTags){
        return null;
    }


}
