package com.example.engineer.API.Model.Tag;

import com.example.engineer.Model.Tag;

public record TagDTO(
        int id,
        String name,
        double value,
        String description,
        boolean hidden,
        int timesUsed
){
    public TagDTO(Tag tag) {
        this(
                tag.getId(),
                tag.getName(),
                tag.getValue(),
                tag.getDescription(),
                tag.isDeleted(),
                tag.getFrames().size()
        );
    }

    public TagDTO(Object[] data){
        this(
                ((Number) data[5]).intValue(),
                (String) data[0],
                ((Number) data[2]).doubleValue(),
                (String) data[3],
                (boolean) data[4],
                ((Number) data[1]).intValue()
        );
    }
}
