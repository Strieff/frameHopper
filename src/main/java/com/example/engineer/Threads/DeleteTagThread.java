package com.example.engineer.Threads;

import com.example.engineer.Model.Tag;
import com.example.engineer.Service.TagService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        });

        while(!executorService.isTerminated()){}

        executorService.close();
    }
}
