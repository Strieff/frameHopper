package com.FrameHopper.app.API.Mapper;

import com.FrameHopper.app.API.Model.Tag.SimpleTagDTO;
import com.FrameHopper.app.Model.Tag;
import org.hibernate.LazyInitializationException;

import java.util.ArrayList;
import java.util.List;

public class TagMapper {
    public static List<SimpleTagDTO> mapTags(List<Tag> tags) {
        try {
            if (tags == null || tags.isEmpty())
                return new ArrayList<>();

            return tags.stream()
                    .map(SimpleTagDTO::new)
                    .toList();
        } catch (LazyInitializationException e) {
            return new ArrayList<>();
        }
    }
}
