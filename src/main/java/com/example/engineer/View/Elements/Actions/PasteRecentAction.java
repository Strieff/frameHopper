package com.example.engineer.View.Elements.Actions;

import com.example.engineer.Service.DataBaseManagementService;
import com.example.engineer.Model.Tag;
import com.example.engineer.Model.Video;
import com.example.engineer.View.Elements.DataManagers.TagListManager;
import com.example.engineer.View.Elements.UpdateTableEvent.UpdateTableEventDispatcher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PasteRecentAction extends ActionHandler{
    private final UndoRedoAction undoRedoAction;
    private final DataBaseManagementService dbService;

    public PasteRecentAction(
            TagListManager tagList,
            UndoRedoAction undoRedoAction,
            DataBaseManagementService dbService
    ) {
        this.undoRedoAction = undoRedoAction;
        this.dbService = dbService;
        this.tagList = tagList;
        tagIdList = new ArrayList<>();
    }

    @Override
    public void performAction(List<Tag> existingTags,int currentFrameIndex, Video video) {
        // Filter tags that are not held in existingTags
        List<Tag> newTags = filterTagList(existingTags);

        // Add filtered tags if there are any new ones
        if (!newTags.isEmpty()) {
            List<Tag> temp = new ArrayList<>(existingTags);
            existingTags.addAll(newTags);
            existingTags.removeAll(temp);
            undoRedoAction.setUp(temp,existingTags,currentFrameIndex, video.getId());
            UpdateTableEventDispatcher.fireEvent();
            dbService.modifyTagsOfFrame(existingTags, currentFrameIndex, video.getId());

        }
    }

    @Override
    protected List<Tag> filterTagList(List<Tag> existingTags) {
        return getTagList().stream()
                .filter(tag -> existingTags.stream().noneMatch(existingTag -> existingTag.equals(tag)))
                .toList();
    }
}
