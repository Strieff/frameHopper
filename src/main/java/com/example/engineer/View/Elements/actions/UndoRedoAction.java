package com.example.engineer.View.Elements.actions;

import com.example.engineer.DBActions.TagManagerAction;
import com.example.engineer.Model.Tag;
import com.example.engineer.Service.FrameService;
import com.example.engineer.View.Elements.UpdateTableEvent.UpdateTableEventDispatcher;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class UndoRedoAction extends ActionHandler implements ApplicationContextAware {
    private static ApplicationContext ctx;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        ctx = applicationContext;
    }

    boolean undid;
    String videoName;

    @Getter
    int currentFrameIndex;
    @Getter
    List<Tag> originalTags;
    @Getter
    List<Tag> currentTags;

    @PostConstruct
    public void init() {
        undid = false;
        frameService = ctx.getBean(FrameService.class);
    }

    public void setUp(List<Tag> originalTags, List<Tag> currentTags, int currentFrameIndex, String videoName){
        this.originalTags = originalTags;
        this.currentFrameIndex = currentFrameIndex;
        this.videoName = videoName;
        this.currentTags = currentTags;
        undid = false;
    }

    public void undoAction(){
        if(!undid){
            UpdateTableEventDispatcher.fireEvent();
            new TagManagerAction(frameService,originalTags,currentFrameIndex,videoName).run();

            flipState();
        }
    }

     public void redoAction(){
         if(undid){
             UpdateTableEventDispatcher.fireEvent();
             new TagManagerAction(frameService,currentTags,currentFrameIndex,videoName).run();

             flipState();
         }
     }

     private void flipState(){
         undid = !undid;
     }
}
