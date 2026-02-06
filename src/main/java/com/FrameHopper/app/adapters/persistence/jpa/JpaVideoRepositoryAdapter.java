package com.FrameHopper.app.adapters.persistence.jpa;

import com.FrameHopper.app.adapters.persistence.mappers.VideoMapper;
import com.FrameHopper.app.adapters.persistence.repository.VideoRepository;
import com.FrameHopper.app.core.domain.Video;
import com.FrameHopper.app.core.ports.out.repository.VideoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JpaVideoRepositoryAdapter implements VideoRepositoryPort {
    private final VideoRepository videoRepository;

    @Override
    public Video getById(int id) {
        return null;
    }

    @Override
    public Video getByPath(String path) {
        var entity = videoRepository.getVideoEntityByPath(path);

        var test = entity.map(VideoMapper::toDomain).orElse(null);


        return test;
    }

    @Override
    public List<Video> getAll() {
        return List.of();
    }

    @Override
    public Video create(Video video) {
        var entity = videoRepository.save(VideoMapper.fromDomain(video));

        return VideoMapper.toDomain(entity);
    }

    @Override
    public Video update(Video video) {
        return null;
    }

    @Override
    public void delete(int id) {

    }
}
