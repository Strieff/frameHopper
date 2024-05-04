package com.example.engineer.Threads;

import com.example.engineer.Model.Tag;
import com.example.engineer.Service.TagService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DeleteTagThread {
    TagService tagService;
    Tag tag;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public DeleteTagThread(TagService tagService, Tag tag) {
        this.tagService = tagService;
        this.tag = tag;
    }

    public void start(){
        executorService.execute(() -> {
            tagService.deleteTag(tag);

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
