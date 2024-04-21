package com.example.engineer.Threads;

import com.example.engineer.Model.Tag;
import com.example.engineer.Service.TagService;
import com.example.engineer.View.FrameHopperView;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeleteTagThread {
    TagService tagService;
    Tag tag;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public DeleteTagThread setUp(TagService tagService,Tag tag){
        this.tagService = tagService;
        this.tag = tag;

        return this;
    }

    public void start(){
        executorService.execute(() -> tagService.deleteTag(tag));
    }
}
