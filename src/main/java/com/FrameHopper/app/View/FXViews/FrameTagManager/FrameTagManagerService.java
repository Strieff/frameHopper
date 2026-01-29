package com.FrameHopper.app.View.FXViews.FrameTagManager;

import com.FrameHopper.app.Service.DataBaseManagementService;
import com.FrameHopper.app.Model.Tag;
import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.View.Elements.DataManagers.TagListManager;
import com.FrameHopper.app.View.Elements.UpdateTableEvent.UpdateTableEventDispatcher;
import com.FrameHopper.app.settings.UserSettingsService;
import com.FrameHopper.app.View.Elements.Actions.PasteRecentAction;
import com.FrameHopper.app.View.Elements.Actions.RemoveRecentAction;
import com.FrameHopper.app.View.Elements.Actions.UndoRedoAction;
import com.FrameHopper.app.View.FXViews.MainView.MainViewService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FrameTagManagerService {
    private final MainViewService mainViewService;
    private final TagListManager tagList;
    private final UserSettingsService userSettingsService;
    private final DataBaseManagementService dbService;

    private InformationContainer info;
    private final ActionContainer actions;

    public FrameTagManagerService(
            PasteRecentAction pasteRecentAction,
            MainViewService mainViewService,
            TagListManager tagList,
            UserSettingsService userSettingsService,
            RemoveRecentAction removeRecentAction,
            UndoRedoAction undoRedoAction,
            DataBaseManagementService dbService
    ) {
        this.mainViewService = mainViewService;
        this.tagList = tagList;
        this.userSettingsService = userSettingsService;
        this.dbService = dbService;

        actions = new ActionContainer(
                pasteRecentAction,
                removeRecentAction,
                undoRedoAction
        );
    }

    public ObservableList<TableEntry> getTags() {
        var heldTagsDTOList = mainViewService.getTagsOnFrame();
        if (heldTagsDTOList == null)
            heldTagsDTOList = new ArrayList<>();
        info = new InformationContainer(heldTagsDTOList);

        ObservableList<TableEntry> data = FXCollections.observableArrayList();

        var allTagsDTOList = tagList != null ? tagList.getTagList() : new ArrayList<Tag>();
        if (!allTagsDTOList.isEmpty()) {
            for (var t : allTagsDTOList) {
                if (userSettingsService.ShowHidden() || !t.isDeleted()) {
                    var selected = heldTagsDTOList.stream().filter(tag -> tag.equals(t)).findFirst().orElse(null);
                    var name = t.isDeleted() ? t.getName() + Dictionary.get("hidden") : t.getName();

                    data.add(new TableEntry(
                            t.getId(),
                            selected != null,
                            name,
                            t.getValue()
                    ));
                }
            }
        }

        return data;
    }

    public void save(){
        actions.setUp(
                info.getOriginalTags(),
                info.getCurrentTags(),
                mainViewService.getCurrentIndex(),
                mainViewService.getCurrentId()
        );

        dbService.modifyTagsOfFrame(
                info.getCurrentTags(),
                info.getOriginalTags(),
                mainViewService.getCurrentIndex(),
                mainViewService.getCurrentId()
        );

        mainViewService.setCurrentTags(info.getCurrentTags());
        close();
        UpdateTableEventDispatcher.fireEvent();
    }

    public void close() {
        info=null;
    }

    public void add(int id){
        info.addTag(getTagById(id));
        actions.addAddRecent(id);
    }

    public void remove(int id){
        info.removeTag(getTagById(id));
        actions.addRemoveRecent(id);
    }

    private Tag getTagById(int id){
        return tagList.getTagList().stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public ObservableList<TableEntry> getFiltered(ObservableList<TableEntry> items, String text) {
        return items.filtered(item -> item.getName().toLowerCase().contains(text.toLowerCase()));
    }

    //class to hold necessary variables
    @Getter
    private static class InformationContainer{
        List<Tag> currentTags;
        final List<Tag> originalTags;
        @Setter
        boolean search;
        @Setter
        String searchString;

        public InformationContainer(List<Tag> tags) {
            currentTags = new ArrayList<>(tags);
            originalTags = new ArrayList<>(tags);
            search = false;
            searchString = "";
        }

        public void addTag(Tag t){
            currentTags.add(t);
        }

        public void removeTag(Tag t){
            currentTags.remove(t);
        }
    }

    //class to hold action objects
    @Getter
    private static class ActionContainer{
        PasteRecentAction pasteRecentAction;
        RemoveRecentAction removeRecentAction;
        UndoRedoAction undoRedoAction;

        public ActionContainer(PasteRecentAction pasteRecentAction, RemoveRecentAction removeRecentAction, UndoRedoAction undoRedoAction) {
            this.pasteRecentAction = pasteRecentAction;
            this.removeRecentAction = removeRecentAction;
            this.undoRedoAction = undoRedoAction;
        }

        public void addRemoveRecent(int i){
            removeRecentAction.addTag(i);
        }

        public void addAddRecent(int i){
            pasteRecentAction.addTag(i);
        }

        public void setUp(List<Tag> originalTags,List<Tag> currentTags,int currentFrameIndex,int id){
            undoRedoAction.setUp(
                    originalTags,
                    currentTags,
                    currentFrameIndex,
                    id
            );
        }
    }
}
