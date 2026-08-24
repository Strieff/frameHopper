package com.FrameHopper.app.adapters.persistence.mappers;

import com.FrameHopper.app.adapters.persistence.entities.TagEntity;
import com.FrameHopper.app.core.domain.Tag;

public class TagMapper {
    public static TagEntity fromDomain(Tag tag) {
        var tagEntity = new TagEntity();

        if(tag.getId() != -1)
            tagEntity.setId(tag.getId());
        tagEntity.setName(tag.getName());
        tagEntity.setValue(tag.getValue());
        tagEntity.setDescription(tag.getDescription());
        tagEntity.setVisible(tag.isVisible());

        return tagEntity;
    }

    public static Tag toDomain(TagEntity tagEntity) {
        return new Tag(
                tagEntity.getId(),
                tagEntity.getName(),
                tagEntity.getValue(),
                tagEntity.getDescription(),
                tagEntity.isVisible()
        );
    }
}
