package com.FrameHopper.app.Service;

import com.FrameHopper.app.Model.Comment;
import com.FrameHopper.app.Model.Tag;
import com.FrameHopper.app.Model.Video;
import jakarta.annotation.PreDestroy;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class DataBaseManagementService {
    private final TagService tagService;
    private final FrameService frameService;
    private final CommentService commentService;

    private final ExecutorService executor;
    private final VideoService videoService;

    public DataBaseManagementService(TagService tagService, FrameService frameService, CommentService commentService, VideoService videoService) {
        this.tagService = tagService;
        this.frameService = frameService;
        this.commentService = commentService;
        this.executor = Executors.newCachedThreadPool();
        this.videoService = videoService;
    }

    @Async
    public void deleteTag(Tag tag) {
        deleteTags(List.of(tag));
    }

    @Async
    public void deleteTags(List<Tag> tags) {
        execute(() -> {
            if(tags.size() == 1)
                tagService.deleteTag(tags.getFirst());
            else
                tagService.deleteTag(tags);
        });
    }

    @Async
    public void hiddenStatusChange(int id, boolean hide) {
        execute(() -> tagService.setHiddenStatus(id, hide));
    }

    @Async
    public void modifyTagsOfFrame(List<Tag> currentTags, int frameNo, int id) {
        modifyTagsOfFrame(currentTags, Collections.emptyList(), frameNo, id);
    }

    @Async
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

    @Async
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

    @Async
    @Transactional
    public void deleteCommentAsync(int commentId, int videoId) {
        Video v = videoService.findById(videoId);
        Comment c = commentService.findById(commentId);

        // remove from video (managed collection)
        v.getComments().removeIf(x -> Objects.equals(x.getId(), commentId));

        // reorder (based on listingOrder)
        List<Comment> sorted = v.getComments().stream()
                .sorted(Comparator.comparingInt(Comment::getListingOrder))
                .toList();

        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).setListingOrder(i + 1);
        }

        commentService.delete(c);
        commentService.saveAll(sorted);
        videoService.saveVideo(v);
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
