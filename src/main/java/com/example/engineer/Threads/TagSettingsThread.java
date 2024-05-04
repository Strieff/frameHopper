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
            //save changes to the database
            tagService.editTag(id, name, value, description);

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
