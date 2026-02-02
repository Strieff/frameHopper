package com.FrameHopper.app.adapters.persistence.jpa;

import com.FrameHopper.app.adapters.persistence.mappers.TagMapper;
import com.FrameHopper.app.adapters.persistence.mappers.VideoMapper;
import com.FrameHopper.app.adapters.persistence.repository.FrameRepository;
import com.FrameHopper.app.adapters.persistence.repository.TagRepository;
import com.FrameHopper.app.core.domain.Tag;
import com.FrameHopper.app.core.domain.Video;
import com.FrameHopper.app.core.ports.out.repository.TagRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JpaTagRepositoryAdapter implements TagRepositoryPort {
    private final TagRepository tagRepository;
    private final FrameRepository frameRepository;

    @Override
    public Tag getById(int id) {
        var entity = tagRepository.findTagEntityById((id));

        return TagMapper.toDomain(entity);
    }

    @Override
    public Tag getByName(String name) {
        var entity = tagRepository.findTagEntityByName(name);

        return TagMapper.toDomain(entity);
    }

    @Override
    public List<Tag> getAllByVideo(Video video) {
        var videoEntity = VideoMapper.fromDomain(video);

        return null;//TODO: join on frames
    }

    @Override
    public List<Tag> getTagsOnVideoFrame(Video video, int frame) {
        var videoEntity = VideoMapper.fromDomain(video);
        var frameEntity = frameRepository.getFrameEntityByFrameNumberAndVideoEntity(frame, videoEntity);

        if (frameEntity == null)
            return new ArrayList<>();

        if (frameEntity.getTagEntities() == null || frameEntity.getTagEntities().isEmpty())
            return new ArrayList<>();

        return frameEntity.getTagEntities().stream()
                .map(TagMapper::toDomain)
                .toList();
    }

    @Override
    public List<Tag> getAll() {
        var entities = tagRepository.findAll();

        return entities.stream().map(TagMapper::toDomain).toList();
    }

    @Override
    public Tag create(Tag tag) {
        var savedEntity = tagRepository.save(TagMapper.fromDomain(tag));

        return TagMapper.toDomain(savedEntity);
    }

    @Override
    public Tag update(Tag tag) {
        var updatedEntity = tagRepository.save(TagMapper.fromDomain(tag));

        return TagMapper.toDomain(updatedEntity);
    }

    @Override
    public Tag updateStatus(int id) {
        var entity = tagRepository.findTagEntityById(id);
        entity.setVisible(!entity.isVisible());

        var updatedEntity = tagRepository.save(entity);

        return TagMapper.toDomain(updatedEntity);
    }

    @Override
    public void delete(int id) {
        tagRepository.deleteById(id);
    }
}
