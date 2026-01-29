package com.FrameHopper.app.View.Elements.Actions;

import com.FrameHopper.app.Service.DataBaseManagementService;
import com.FrameHopper.app.Model.Tag;
import com.FrameHopper.app.Model.Video;
import com.FrameHopper.app.View.Elements.DataManagers.TagListManager;
import com.FrameHopper.app.View.Elements.UpdateTableEvent.UpdateTableEventDispatcher;
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
