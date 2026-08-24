package com.FrameHopper.app.adapters.persistence.mappers;

import com.FrameHopper.app.adapters.persistence.entities.FrameEntity;
import com.FrameHopper.app.core.domain.Frame;
import com.FrameHopper.app.core.domain.Tag;
import com.FrameHopper.app.core.domain.Video;

import java.util.ArrayList;
import java.util.List;

public class FrameMapper {
    public  static FrameEntity fromDomain(Frame frame) {
        var entity = new FrameEntity();

        if(frame.getId() != -1)
            entity.setId(frame.getId());

        entity.setFrameNumber(frame.getFrameNumber());

        var videoEntity = frame.getVideo() == null ? null : VideoMapper.fromDomain(frame.getVideo());
        entity.setVideoEntity(videoEntity);

        var tagEntities = frame.getTags().stream().map(TagMapper::fromDomain).toList();
        entity.setTagEntities(tagEntities);

        return entity;
    }

    public static Frame toDomain(FrameEntity entity) {
        Video video;

        try {
            video = VideoMapper.toDomain(entity.getVideoEntity());
        } catch (Exception ex) {
            video = null;
        }

        List<Tag> tags = entity.getTagEntities().isEmpty() ?
                new ArrayList<>() :
                entity.getTagEntities().stream().map(TagMapper::toDomain).toList();

        return new Frame(
                entity.getId(),
                entity.getFrameNumber(),
                video,
                tags
        );
    }
}
