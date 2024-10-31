package com.example.engineer.View.FXViews.TagManager;

import com.example.engineer.View.Elements.Language.Dictionary;
import com.example.engineer.View.Elements.TagListManager;
import com.example.engineer.View.Elements.UserSettingsManager;
import com.example.engineer.View.FXViews.MainView.MainViewService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TagManagerService {
    @Autowired
    MainViewService mainViewService;
    @Autowired
    TagListManager tagList;
    @Autowired
    UserSettingsManager userSettings;

    public ObservableList<TableEntry> getTags(){
        var heldTagsDTOList = mainViewService.getTagsOnFrame();
        ObservableList<TableEntry> data = FXCollections.observableArrayList();

        if(heldTagsDTOList != null){
            var allTagsDTOList = tagList.getTagList();
            for(var t : allTagsDTOList){
                if(userSettings.ShowHidden() || !t.isDeleted()){
                    var selected = heldTagsDTOList.stream().filter(tag -> tag.equals(t)).findFirst().orElse(null);
                    var name = t.isDeleted() ? t.getName() + Dictionary.get("tm.tag.hidden") : t.getName();

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
}
