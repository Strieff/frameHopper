package com.example.engineer.View.FXViews.TagDetails;

import com.example.engineer.Model.Tag;
import com.example.engineer.Service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TagDetailsService {
    @Autowired
    private TagService tagService;

    public Tag getTag(int id) {
        return tagService.getTag(id);
    }
}
