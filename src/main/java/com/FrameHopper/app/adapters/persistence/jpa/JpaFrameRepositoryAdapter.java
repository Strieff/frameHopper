package com.FrameHopper.app.adapters.persistence.jpa;

import com.FrameHopper.app.adapters.persistence.mappers.FrameMapper;
import com.FrameHopper.app.adapters.persistence.mappers.VideoMapper;
import com.FrameHopper.app.adapters.persistence.repository.FrameRepository;
import com.FrameHopper.app.core.domain.Frame;
import com.FrameHopper.app.core.domain.Video;
import com.FrameHopper.app.core.ports.out.repository.FrameRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JpaFrameRepositoryAdapter implements FrameRepositoryPort {
    private final FrameRepository frameRepository;

    @Override
    public List<Frame> getAllFramesOnVideo(Video video) {
        var videoEntity = VideoMapper.fromDomain(video);
        var entities = frameRepository.getFrameEntitiesByVideoEntity(videoEntity);

        return entities.isEmpty() ? new ArrayList<>() : entities.stream().map(FrameMapper::toDomain).toList();
    }

    @Override
    public Frame getFrameById(int id) {
        var entity = frameRepository.findFrameEntityById(id);

        return entity.map(FrameMapper::toDomain).orElse(null);
    }

    @Override
    public List<Frame> getAll() {
        var entities = frameRepository.findAll();

        if(entities.isEmpty())
            return new ArrayList<>();

        return entities.stream().map(FrameMapper::toDomain).toList();
    }

    @Override
    public Frame create(Frame frame) {
        var entity = frameRepository.save(FrameMapper.fromDomain(frame)) ;

        return FrameMapper.toDomain(entity);
    }

    @Override
    public Frame update(Frame frame) {
        var entity = frameRepository.save(FrameMapper.fromDomain(frame));

        return FrameMapper.toDomain(entity);
    }

    @Override
    public void delete(int id) {
        frameRepository.deleteById(id);
    }


}
