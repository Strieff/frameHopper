package com.example.engineer.View.FXViews.Settings;

import com.example.engineer.Model.Tag;
import com.example.engineer.Model.Video;
import com.example.engineer.Service.VideoService;
import com.example.engineer.View.Elements.DataManagers.OpenViewsInformationContainer;
import com.example.engineer.View.Elements.DataManagers.TagListManager;
import com.example.engineer.View.Elements.Language.Dictionary;
import com.example.engineer.View.Elements.Language.LanguageEntry;
import com.example.engineer.View.Elements.Language.LanguageManager;
import com.example.engineer.View.Elements.UpdateTableEvent.UpdateTableEventDispatcher;
import com.example.engineer.View.Elements.DataManagers.UserSettingsManager;
import com.example.engineer.View.FXViews.MainView.MainViewService;
import jakarta.annotation.PostConstruct;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class SettingsService {
    @Autowired
    OpenViewsInformationContainer openViews;
    @Autowired
    TagListManager tagList;
    @Autowired
    UserSettingsManager userSettings;
    @Autowired
    MainViewService mainViewService;
    @Autowired
    private VideoService videoService;
    @Autowired
    LanguageManager languageManager;

    @Getter
    private List<LanguageEntry> languages = new ArrayList<>();

    @PostConstruct
    public void init() {
        var languageMap = languageManager.getLanguageMap();
        for(var e : languageMap.keySet())
            languages.add(new LanguageEntry(e, languageMap.get(e)));
    }

    public void close() {

    }

    public ObservableList<TableEntry> getExistingTags(){
        ObservableList<TableEntry> data = FXCollections.observableArrayList();

        for(var t : tagList.getTagList())
            if(userSettings.ShowHidden() || !t.isDeleted())
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

    public Video getCurrentVideo() {
        return videoService.getById(mainViewService.getCurrentId());
    }

    public void changeShowHidden(boolean checked){
        userSettings.setShowHidden(checked);
        UpdateTableEventDispatcher.fireEvent();
    }

    public LanguageEntry getCurrentLanguage() {
        return languages.stream()
                .filter(e -> e.getCode().equals(userSettings.getLanguage()))
                .findFirst()
                .orElse(null);
    }
}
