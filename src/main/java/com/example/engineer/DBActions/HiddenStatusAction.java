package com.example.engineer.DBActions;

import com.example.engineer.Service.TagService;

import java.util.concurrent.CompletableFuture;

public class HiddenStatusAction extends DBAction{
    TagService tagService;
    int id;
    boolean hide;

    public HiddenStatusAction(TagService tagService, int id, boolean hide) {
        super();
        this.tagService = tagService;
        this.id = id;
        this.hide = hide;
    }

    @Override
    public void run() {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            tagService.setHiddenStatus(id, hide);
        },executor);

        future.join();

        shutdown();
    }
}
