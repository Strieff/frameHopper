package com.FrameHopper.app.boundry.mappers;

import com.FrameHopper.app.boundry.dto.FrameDTO;
import com.FrameHopper.app.boundry.dto.TagDTO;
import com.FrameHopper.app.core.domain.Frame;
import com.FrameHopper.app.core.domain.Tag;

import java.util.ArrayList;
import java.util.LinkedList;

public class BoundaryFrameMapper {
    public static FrameDTO fromDomain(Frame frame) {
        var tags = frame.getTags() == null || frame.getTags().isEmpty() ?
                new ArrayList<TagDTO>() :
                new LinkedList<>(frame.getTags().stream().map(BoundaryTagMapper::fromDomain).toList());

        return new FrameDTO(
                frame.getId(),
                frame.getFrameNumber(),
                frame.getVideo() == null ? null : BoundaryVideoMapper.fromDomain(frame.getVideo()),
                tags
        );
    }

    public static Frame toDomain(FrameDTO frameDTO) {
        var tags = frameDTO.tags() == null || frameDTO.tags().isEmpty() ?
                new ArrayList<Tag>() :
                frameDTO.tags().stream().map(BoundaryTagMapper::toDomain).toList();

        return new Frame(
                frameDTO.id(),
                frameDTO.frameNumber(),
                frameDTO.video() == null ? null : BoundaryVideoMapper.toDomain(frameDTO.video()),
                tags
        );
    }
}
