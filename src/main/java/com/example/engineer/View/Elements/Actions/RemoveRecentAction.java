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
public class RemoveRecentAction extends ActionHandler{
    private final UndoRedoAction undoRedoAction;
    private final DataBaseManagementService dbService;

    public RemoveRecentAction(TagListManager tagList,
                              UndoRedoAction undoRedoAction,
                              DataBaseManagementService dbService
    ) {
        this.undoRedoAction = undoRedoAction;
        this.dbService = dbService;
        this.tagList = tagList;
        this.tagIdList = new ArrayList<>();
    }

    @Override
    public void performAction(List<Tag> existingTags, int currentFrameIndex, Video video) {
        // Filter tags that are not held in existingTags
        List<Tag> removeTags = filterTagList(existingTags);

        if(!removeTags.isEmpty()) {
            List<Tag> temp = new ArrayList<>(existingTags);
            var existingTagsTemp = new ArrayList<>(existingTags);
            existingTagsTemp.removeAll(removeTags);
            undoRedoAction.setUp(temp,existingTags,currentFrameIndex, video.getId());
            dbService.modifyTagsOfFrame(existingTagsTemp, existingTags,currentFrameIndex, video.getId());
            existingTags.removeAll(removeTags);
            UpdateTableEventDispatcher.fireEvent();
        }
    }

    @Override
    protected List<Tag> filterTagList(List<Tag> existingTags) {
                return getTagList().stream()
                .filter(existingTags::contains)
                .toList();
    }
}
