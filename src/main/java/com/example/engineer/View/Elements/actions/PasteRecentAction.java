package com.example.engineer.View.Elements.actions;

import com.example.engineer.Model.Tag;
import com.example.engineer.Model.Video;
import com.example.engineer.Service.FrameService;
import com.example.engineer.DBActions.TagManagerAction;
import com.example.engineer.View.Elements.TagListManager;
import com.example.engineer.View.Elements.UpdateTableEvent.UpdateTableEventDispatcher;
import jakarta.annotation.PostConstruct;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PasteRecentAction extends ActionHandler implements ApplicationContextAware{
    private static ApplicationContext ctx;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        ctx = applicationContext;
    }

    @PostConstruct
    public void init(){
        this.tagList =  ctx.getBean(TagListManager.class);
        this.tagIdList = new ArrayList<>();
        this.frameService = ctx.getBean(FrameService.class);
    }

    @Override
    public void performAction(List<Tag> existingTags,int currentFrameIndex, Video video) {
        // Filter tags that are not held in existingTags
        List<Tag> newTags = filterTagList(existingTags);

        // Add filtered tags if there are any new ones
        if (!newTags.isEmpty()) {
            List<Tag> temp = new ArrayList<>(existingTags);
            existingTags.addAll(newTags);
            ctx.getBean(UndoRedoAction.class).setUp(temp,existingTags,currentFrameIndex, video.getName());
            UpdateTableEventDispatcher.fireEvent();
            new TagManagerAction(frameService,existingTags,currentFrameIndex,video.getName()).run();
        }
    }

    @Override
    protected List<Tag> filterTagList(List<Tag> existingTags) {
        return getTagList().stream()
                .filter(tag -> existingTags.stream().noneMatch(existingTag -> existingTag.equals(tag)))
                .toList();
    }
}
