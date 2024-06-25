package com.example.engineer.DBActions;

import com.example.engineer.Service.TagService;

import java.util.concurrent.CompletableFuture;

public class TagSettingsAction extends DBAction{
    TagService tagService;
    int id;
    String name,description;
    double value;

    public TagSettingsAction(TagService tagService, int id, String name, String description, double value) {
        super();
        this.tagService = tagService;
        this.id = id;
        this.name = name;
        this.description = description;
        this.value = value;
    }

    @Override
    public void run() {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            tagService.editTag(id,name,value,description);
        },executor);

        future.join();

        shutdown();
    }
}
