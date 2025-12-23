package com.example.engineer.API.Model;

import com.example.engineer.API.Mapper.TagMapper;
import com.example.engineer.API.Model.Tag.SimpleTagDTO;
import com.example.engineer.Model.Frame;

import java.util.List;

public record FrameDTO(
        int id,
        int frameNumber,
        List<SimpleTagDTO> tags
) {
    public FrameDTO(Frame frame) {
        this(
                frame.getId(),
                frame.getFrameNumber(),
                TagMapper.mapTags(frame.getTags())
        );
    }
}
