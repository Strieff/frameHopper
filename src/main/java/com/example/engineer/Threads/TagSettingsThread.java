package com.example.engineer.Threads;

import com.example.engineer.Model.Tag;
import com.example.engineer.Service.TagService;
import com.example.engineer.View.FrameHopperView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TagSettingsThread {
    TagService tagService;
    Integer id;
    String name, description;
    Double value;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public TagSettingsThread(TagService tagService, Integer id, String name, Double value, String description){
        this.tagService = tagService;
        this.id = id;
        this.name = name;
        this.value = value;
        this.description = description;
    }

    public void start() {
        executorService.execute(() -> {
            if (id == null) {
                //add tag to database and the list
                FrameHopperView.TAG_LIST.add(tagService.createTag(name, value, description));
            } else {
                //save changes to the database
                Tag t = tagService.editTag(id, name, value, description);

                int index = FrameHopperView.findTagIndexById(id);
                FrameHopperView.TAG_LIST.set(index, t);
            }

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
}
