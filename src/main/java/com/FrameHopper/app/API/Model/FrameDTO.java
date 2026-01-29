package com.FrameHopper.app.API.Model;

import com.FrameHopper.app.API.Mapper.TagMapper;
import com.FrameHopper.app.API.Model.Tag.SimpleTagDTO;
import com.FrameHopper.app.Model.Frame;

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
