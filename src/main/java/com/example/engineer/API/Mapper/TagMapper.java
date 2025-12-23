package com.example.engineer.API.Mapper;

import com.example.engineer.API.Model.Tag.SimpleTagDTO;
import com.example.engineer.Model.Tag;
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
