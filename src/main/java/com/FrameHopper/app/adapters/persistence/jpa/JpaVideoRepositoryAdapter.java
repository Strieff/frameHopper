package com.FrameHopper.app.adapters.persistence.jpa;

import com.FrameHopper.app.adapters.persistence.mappers.VideoMapper;
import com.FrameHopper.app.adapters.persistence.repository.VideoRepository;
import com.FrameHopper.app.core.domain.Video;
import com.FrameHopper.app.core.ports.out.repository.VideoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JpaVideoRepositoryAdapter implements VideoRepositoryPort {
    private final VideoRepository videoRepository;

    @Override
    public Video getById(int id) {
        var entity = videoRepository.findVideoEntitiesById(id);

        return entity.map(VideoMapper::toDomain).orElse(null);
    }

    @Override
    public Video getByPath(String path) {
        var entity = videoRepository.getVideoEntityByPath(path);

        return entity.map(VideoMapper::toDomain).orElse(null);
    }

    @Override
    public List<Video> getAll() {
        var entities = videoRepository.findAll();

        if(entities.isEmpty())
            return new ArrayList<>();

        return entities.stream().map(VideoMapper::toDomain).toList();
    }

    @Override
    public List<Video> getAllWithNotes() {
        var entities = videoRepository.getVideoEntitiesWithNotes();

        if(entities.isEmpty())
            return new ArrayList<>();

        return entities.stream().map(VideoMapper::toDomain).toList();
    }

    @Override
    public Video create(Video video) {
        var entity = videoRepository.save(VideoMapper.fromDomain(video));

        return VideoMapper.toDomain(entity);
    }

    @Override
    public Video update(Video video) {
        var updatedEntity = videoRepository.save(VideoMapper.fromDomain(video));

        return VideoMapper.toDomain(updatedEntity);
    }

    @Override
    public void delete(int id) {
        videoRepository.deleteById(id);
    }
}
