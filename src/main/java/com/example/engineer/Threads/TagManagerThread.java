package com.example.engineer.Threads;

import com.example.engineer.Model.Tag;
import com.example.engineer.Service.FrameService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class TagManagerThread{
    FrameService frameService;
    List<Tag> currentTags = null;
    List<Tag> originalTags = null;
    int frameNo = -1;
    String videoName = null;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public TagManagerThread setUp(List<Tag> currentTags, List<Tag> originalTags, int frameNo, String videoName,FrameService frameService){
        this.currentTags = currentTags;
        this.originalTags = originalTags;
        this.frameNo = frameNo;
        this.videoName = videoName;
        this.frameService = frameService;

        return this;
    }

    public void start(){
        executorService.execute(() -> {
            if(currentTags.size() == 0 && originalTags.size() != 0)
                frameService.modifyTagsOfFrame(new ArrayList<>(),frameNo,videoName);

            if(
                    (currentTags.size() != 0 && originalTags.size() == 0) ||
                    (currentTags.size() != 0 && currentTags.size() != originalTags.size())
            ){
                frameService.modifyTagsOfFrame(currentTags,frameNo,videoName);
            }

            if(compareTagListsWhenEqualLen())
                frameService.modifyTagsOfFrame(currentTags,frameNo,videoName);

            executorService.shutdown();

            try {
                if(!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)){
                    executorService.shutdownNow();
                }
            }catch (Exception e){
                executorService.shutdownNow();
            }
        });
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
