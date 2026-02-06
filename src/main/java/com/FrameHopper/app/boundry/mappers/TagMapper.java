package com.FrameHopper.app.boundry.mappers;

import com.FrameHopper.app.core.domain.Tag;
import com.FrameHopper.app.boundry.dto.TagDTO;

public class TagMapper {
    public static Tag toDomain(TagDTO tagDTO) {
        return new Tag(
                tagDTO.getId(),
                tagDTO.getName(),
                tagDTO.getValue(),
                tagDTO.getDescription(),
                tagDTO.isVisible()
        );
    }

    public static TagDTO fromDomain(Tag tag) {
        return new TagDTO(
                tag.getId(),
                tag.getName(),
                tag.getValue(),
                tag.getDescription(),
                tag.isVisible()
        );
    }
}
