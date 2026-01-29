package com.FrameHopper.app.View.FXViews.TagManager;

import com.FrameHopper.app.Model.Tag;
import com.FrameHopper.app.View.Elements.DataManagers.TagListManager;
import com.FrameHopper.app.settings.UserSettingsService;
import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.View.Elements.UpdateTableEvent.UpdateTableEventDispatcher;
import com.FrameHopper.app.View.FXViews.MainView.MainViewService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class TagManagerService {
    private final TagListManager tagList;
    private final MainViewService mainViewService;
    private final UserSettingsService userSettingsService;

    public TagManagerService(
            TagListManager tagList,
            MainViewService mainViewService,
            UserSettingsService userSettingsService
    ) {
        this.tagList = tagList;
        this.mainViewService = mainViewService;
        this.userSettingsService = userSettingsService;
    }

    public ObservableList<TableEntry> getExistingTags(){
        ObservableList<TableEntry> data = FXCollections.observableArrayList();

        for(var t : tagList.getTagList())
            if(userSettingsService.ShowHidden() || !t.isDeleted())
                data.add(new TableEntry(
                        t.getId(),
                        t.isDeleted() ? t.getName() + Dictionary.get("hidden") : t.getName(),
                        t.getValue(),
                        t.getDescription()
                ));

        return data;
    }

    public void removeTag(int id){
        var tag = tagList.getTag(id);
        mainViewService.deleteTag(tag);
        tagList.removeTag(id);
        UpdateTableEventDispatcher.fireEvent();
    }

    public void removeTags(List<Integer> ids){
        var tags = new ArrayList<Tag>();
        for(var id : ids)
            tags.add(tagList.getTag(id));

        mainViewService.deleteTags(tags);
        tagList.removeTags(tags);
        UpdateTableEventDispatcher.fireEvent();
    }

    public void loadBatchTags(String path) {
        try {
            var list = new ArrayList<Tag>();
            var lines = Files.readAllLines(Path.of(path));

            for(var line : lines){
                var data = line.split(";");

                //check if tag is correct length
                if(data.length<2 || data.length>3)
                    throw new Exception(String.format(Dictionary.get("tag.invalid"),line));

                //check if name is not empty
                if(data[0].isBlank())
                    throw new Exception(Dictionary.get("tag.name.empty"));

                //check if tag name exists
                if(tagList.getTag(data[0]) != null)
                    throw new Exception(String.format(Dictionary.get("tag.name.exists"),data[0]));

                //check if value is not empty
                if(data[1].isBlank())
                    throw new Exception(Dictionary.get("tag.value.empty"));

                //check if value is an integer
                try{
                    Double.parseDouble(data[1]);
                }catch(NumberFormatException e){
                    throw new Exception(Dictionary.get("tag.value.nan"));
                }

                //check if value is a positive number
                if(Double.parseDouble(data[1])<0)
                    throw new Exception(Dictionary.get("tag.value.non-positive"));

                list.add(Tag.builder()
                        .name(data[0])
                        .value(Double.parseDouble(data[1]))
                        .description(data.length == 3 ? data[2] : "")
                        .build());
            }

            tagList.addTags(list);
            UpdateTableEventDispatcher.fireEvent();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void hideTags(List<Integer> ids){
        changeHiddenStatus(ids,true);
    }

    public void unhideTags(List<Integer> ids){
        changeHiddenStatus(ids,false);
    }

    private void changeHiddenStatus(List<Integer> ids, boolean hide){
        for(var id : ids)
            tagList.changeHideStatus(id,hide);
        UpdateTableEventDispatcher.fireEvent();
    }
}
