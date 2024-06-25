package com.example.engineer.DBActions;

import com.example.engineer.Model.Tag;
import com.example.engineer.Service.TagService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DeleteTagAction extends DBAction{
    TagService tagService;
    List<Tag> tags;

    public DeleteTagAction(TagService tagService, Tag tag) {
        super();
        this.tagService = tagService;
        this.tags = List.of(tag);
    }

    public DeleteTagAction(TagService tagService, List<Tag> tags) {
        super();
        this.tagService = tagService;
        this.tags = tags;
    }

    @Override
    public void run() {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            if(tags.size() == 1)
                tagService.deleteTag(tags.get(0));
            else
                tagService.deleteTag(tags);
        },executor);

        future.join();

        shutdown();
    }
}
