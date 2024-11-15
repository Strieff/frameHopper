package com.example.engineer.View.FXViews.TagManager;

import com.example.engineer.DBActions.TagManagerAction;
import com.example.engineer.Model.Tag;
import com.example.engineer.Service.FrameService;
import com.example.engineer.View.Elements.Language.Dictionary;
import com.example.engineer.View.Elements.DataManagers.TagListManager;
import com.example.engineer.View.Elements.UpdateTableEvent.UpdateTableEventDispatcher;
import com.example.engineer.View.Elements.DataManagers.UserSettingsManager;
import com.example.engineer.View.Elements.Actions.PasteRecentAction;
import com.example.engineer.View.Elements.Actions.RemoveRecentAction;
import com.example.engineer.View.Elements.Actions.UndoRedoAction;
import com.example.engineer.View.FXViews.MainView.MainViewService;
import jakarta.annotation.PostConstruct;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TagManagerService {
    @Autowired
    MainViewService mainViewService;
    @Autowired
    TagListManager tagList;
    @Autowired
    UserSettingsManager userSettings;
    @Autowired
    private FrameService frameService;
    @Autowired
    private PasteRecentAction pasteRecentAction;
    @Autowired
    private RemoveRecentAction removeRecentAction;
    @Autowired
    private UndoRedoAction undoRedoAction;

    private InformationContainer info;
    private ActionContainer actions;

    @PostConstruct
    public void init(){
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
                if (userSettings.ShowHidden() || !t.isDeleted()) {
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
                mainViewService.getCurrentPath()
        );

        new TagManagerAction(
                frameService,
                info.getCurrentTags(),
                info.getOriginalTags(),
                mainViewService.getCurrentIndex(),
                mainViewService.getCurrentPath()
        ).run();

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
    private static class InformationContainer{
        @Getter
        List<Tag> currentTags;
        @Getter
        final List<Tag> originalTags;
        @Getter
        @Setter
        boolean search;
        @Getter
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
    private static class ActionContainer{
        @Getter
        PasteRecentAction pasteRecentAction;
        @Getter
        RemoveRecentAction removeRecentAction;
        @Getter
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

        public void setUp(List<Tag> originalTags,List<Tag> currentTags,int currentFrameIndex,String videoPath){
            undoRedoAction.setUp(
                    originalTags,
                    currentTags,
                    currentFrameIndex,
                    videoPath
            );
        }
    }
}
