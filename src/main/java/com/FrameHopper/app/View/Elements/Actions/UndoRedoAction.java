package com.FrameHopper.app.View.Elements.Actions;

import com.FrameHopper.app.Service.DataBaseManagementService;
import com.FrameHopper.app.Model.Tag;
import com.FrameHopper.app.View.Elements.UpdateTableEvent.UpdateTableEventDispatcher;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class UndoRedoAction extends ActionHandler{
    Boolean undid;
    Integer id;

    private final DataBaseManagementService dbService;

    @Getter
    int currentFrameIndex;
    @Getter
    List<Tag> originalTags;
    @Getter
    List<Tag> currentTags;

    public UndoRedoAction(DataBaseManagementService dbService) {
        this.dbService = dbService;
        undid = false;
    }

    public void setUp(List<Tag> originalTags, List<Tag> currentTags, int currentFrameIndex, int id){
        this.originalTags = originalTags;
        this.currentFrameIndex = currentFrameIndex;
        this.id = id;
        this.currentTags = currentTags;
        undid = false;
    }

    public void undoAction(){
        if(!undid){
            UpdateTableEventDispatcher.fireEvent();
            dbService.modifyTagsOfFrame(originalTags, currentFrameIndex, id);

            flipState();
        }
    }

     public void redoAction(){
         if(undid){
             UpdateTableEventDispatcher.fireEvent();
             dbService.modifyTagsOfFrame(currentTags, currentFrameIndex, id);

             flipState();
         }
     }

     private void flipState(){
         undid = !undid;
     }
}
