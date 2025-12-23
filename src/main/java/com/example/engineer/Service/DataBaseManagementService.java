package com.example.engineer.Service;

import com.example.engineer.Model.Tag;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class DataBaseManagementService {
    private final TagService tagService;
    private final FrameService frameService;

    private final ExecutorService executor;

    public DataBaseManagementService(TagService tagService, FrameService frameService) {
        this.tagService = tagService;
        this.frameService = frameService;
        this.executor = Executors.newCachedThreadPool();
    }

    public void deleteTag(Tag tag) {
        deleteTags(List.of(tag));
    }

    public void deleteTags(List<Tag> tags) {
        execute(() -> {
            if(tags.size() == 1)
                tagService.deleteTag(tags.getFirst());
            else
                tagService.deleteTag(tags);
        });
    }

    public void hiddenStatusChange(int id, boolean hide) {
        execute(() -> tagService.setHiddenStatus(id, hide));
    }

    public void modifyTagsOfFrame(List<Tag> currentTags, int frameNo, int id) {
        modifyTagsOfFrame(currentTags, Collections.emptyList(), frameNo, id);
    }

    public void modifyTagsOfFrame(List<Tag> currentTags, List<Tag> originalTags, int frameNo, int id) {
        execute(() -> {
            if(originalTags!=null){
                if (currentTags.isEmpty() && !originalTags.isEmpty())
                    frameService.modifyTagsOfFrame(new ArrayList<>(), frameNo, id);

                if ((!currentTags.isEmpty() && originalTags.isEmpty()) || (!currentTags.isEmpty() && currentTags.size() != originalTags.size()))
                    frameService.modifyTagsOfFrame(currentTags, frameNo, id);

                if (compareTagListsWhenEqualLen(currentTags, originalTags))
                    frameService.modifyTagsOfFrame(currentTags, frameNo, id);
            }else
                frameService.modifyTagsOfFrame(currentTags,frameNo, id);
        });
    }

    public void editTag(int id, String name, String description, double value) {
        execute(() -> tagService.editTag(id,name,value,description));
    }

    private boolean compareTagListsWhenEqualLen(List<Tag> currentTags, List<Tag> originalTags){
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

    private void execute(Runnable task) {
        CompletableFuture.runAsync(task, executor).join();
    }

    @PreDestroy
    public void close() {
        executor.shutdownNow();

        try {
            if(!executor.awaitTermination(800, TimeUnit.MILLISECONDS)){
                executor.shutdownNow();
            }
        }catch (Exception e){
            executor.shutdownNow();
        }
    }
}
