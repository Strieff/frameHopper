package com.example.engineer.View.FXViews.CreateTag;

import com.example.engineer.View.Elements.FXElementsProviders.FXDialogProvider;
import com.example.engineer.View.Elements.DataManagers.TagListManager;
import com.example.engineer.View.Elements.Language.Dictionary;
import com.example.engineer.View.Elements.UpdateTableEvent.UpdateTableEventDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateTagService {
    @Autowired
    TagListManager tagList;

    public void createTag(String tagName, String value,String tagDescription) {
        try{
            //check if name is not empty
            if(tagName.isBlank())
                throw new Exception(Dictionary.get("tag.name.empty"));

            //check if tag name exists
            if(tagList.getTag(tagName) != null)
                throw new Exception(String.format(Dictionary.get("tag.name.exists"),tagName));

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

            tagList.addTag(
                    tagName,
                    Double.parseDouble(value),
                    tagDescription == null ? "" : tagDescription
            );
            UpdateTableEventDispatcher.fireEvent();
        }catch (Exception e){
            FXDialogProvider.errorDialog(e.getMessage());
        }

    }
}
