package com.example.engineer.View.FXViews.TagDetails;

import com.example.engineer.Model.Tag;
import com.example.engineer.Service.TagService;
import com.example.engineer.View.Elements.DataManagers.TagListManager;
import com.example.engineer.View.Elements.UpdateTableEvent.UpdateTableEventDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TagDetailsService {
    @Autowired
    private TagService tagService;
    @Autowired
    private TagListManager tagList;

    public Tag getTag(int id) {
        return tagService.getTag(id);
    }

    public void updateTag(Tag tag,String name, String value, String description) throws Exception{
        if(name.isBlank())
            throw new Exception("Name cannot be blank");

        if(value.isBlank())
            throw new Exception("Value cannot be blank");

        try{
            Double.parseDouble(value);
        }catch (NumberFormatException e){
            throw new Exception("Value must be a number");
        }

        tagList.editTag(
                tag.getId(),
                name,
                Double.parseDouble(value),
                description,
                tag.isDeleted()
        );

        UpdateTableEventDispatcher.fireEvent();
    }
}
