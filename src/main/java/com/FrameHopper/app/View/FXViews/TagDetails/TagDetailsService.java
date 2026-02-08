package com.FrameHopper.app.View.FXViews.TagDetails;

import com.FrameHopper.app.Model.Tag;
import com.FrameHopper.app.Service.TagService;
import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.View.Elements.UpdateTableEvent.UpdateTableEventDispatcher;
import org.springframework.stereotype.Component;

@Component
public class TagDetailsService {
    private final TagService tagService;

    public TagDetailsService(TagService tagService) {
        this.tagService = tagService;
    }

    public Tag getTag(int id) {
        return tagService.getTag(id);
    }

    public void updateTag(Tag tag,String name, String value, String description) throws Exception{
        //check if name is not empty
        if(name.isBlank())
            throw new Exception(Dictionary.get("tag.name.empty"));

        //check if tag name exists
        if(false)
            throw new Exception(String.format(Dictionary.get("tag.name.exists"),name));

        if(value.isBlank())
            throw new Exception(Dictionary.get("tag.value.empty"));

        //check if value is an integer
        try{
            Double.parseDouble(value);
        }catch(NumberFormatException e){
            throw new Exception(Dictionary.get("tag.value.nan"));
        }

        //check if value is a positive number
        if(Double.parseDouble(value)<0)
            throw new Exception(Dictionary.get("tag.value.non-positive"));

        /*tagList.editTag(
                tag.getId(),
                name,
                Double.parseDouble(value),
                description,
                tag.isDeleted()
        );*/

        UpdateTableEventDispatcher.fireEvent();
    }
}
