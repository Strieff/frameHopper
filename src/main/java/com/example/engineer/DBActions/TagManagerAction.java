package com.example.engineer.DBActions;

import com.example.engineer.Model.Tag;
import com.example.engineer.Service.FrameService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TagManagerAction extends DBAction{
    FrameService frameService;
    List<Tag> currentTags;
    List<Tag> originalTags;
    int frameNo;
    String videoName;

    public TagManagerAction(FrameService frameService, List<Tag> currentTags, List<Tag> originalTags, int frameNo, String videoName) {
        super();
        this.frameService = frameService;
        this.currentTags = currentTags;
        this.originalTags = originalTags;
        this.frameNo = frameNo;
        this.videoName = videoName;
    }

    public TagManagerAction(FrameService frameService, List<Tag> currentTags, int frameNo, String videoName) {
        super();
        this.frameService = frameService;
        this.currentTags = currentTags;
        this.frameNo = frameNo;
        this.videoName = videoName;
    }

    @Override
    public void run() {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            if(originalTags!=null){
                if (currentTags.isEmpty() && !originalTags.isEmpty())
                    frameService.modifyTagsOfFrame(new ArrayList<>(), frameNo, videoName);

                if ((!currentTags.isEmpty() && originalTags.isEmpty()) || (!currentTags.isEmpty() && currentTags.size() != originalTags.size()))
                    frameService.modifyTagsOfFrame(currentTags, frameNo, videoName);

                if (compareTagListsWhenEqualLen())
                    frameService.modifyTagsOfFrame(currentTags, frameNo, videoName);
            }else
                frameService.modifyTagsOfFrame(currentTags,frameNo,videoName);
        },executor);

        future.join();

        shutdown();
    }

    private boolean compareTagListsWhenEqualLen(){
        if(currentTags.size() != originalTags.size())
            return false;

        List<String> currentTagsList = new ArrayList<>();
        List<String> originalTagsList = new ArrayList<>();

        for(Tag t : currentTags)
            currentTagsList.add(t.getName());

        for (Tag t : originalTags)
            originalTagsList.add(t.getName());

        currentTagsList.sort(String::compareTo);
        originalTagsList.sort(String::compareTo);

        return !currentTagsList.equals(originalTagsList);
    }
}
